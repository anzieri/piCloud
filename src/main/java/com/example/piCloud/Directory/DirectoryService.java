package com.example.piCloud.Directory;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.piCloud.File.FileEntity;
import com.example.piCloud.File.FileRepository;
import com.example.piCloud.File.FileService;
import com.example.piCloud.User.User;
import com.example.piCloud.User.UserService;
import com.example.piCloud.config.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DirectoryService {
    @Autowired
    private final DirectoryRepository directoryRepository;

    @Value("${file.upload-dir}")
    private String BASE_DIRECTORY;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;

    public DirectoryService(DirectoryRepository directoryRepository, JwtService jwtService, UserService userService, FileRepository fileRepository, FileService fileService) {
        this.directoryRepository = directoryRepository;
        this.jwtService = jwtService;
        this.userService = userService;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }

    // Creates a new directory in the base directory specified by BASE_DIRECTORY.
    public DirectoryEntity createDirectory(String dirName) throws Exception {
        Path dirPath = Paths.get(this.BASE_DIRECTORY, new String[] { dirName });
        Files.createDirectories(dirPath, (FileAttribute<?>[])new FileAttribute[0]);
        DirectoryEntity directory = new DirectoryEntity();
        directory.setName(dirName);
        directory.setPath(dirPath.toString());
        directory.setUser(this.userService.getUserByEmail((
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()));
        this.directoryRepository.save(directory);
        return directory;
    }

    // Edits the name of an existing directory and updates paths of all contained files and subdirectories.
    public void editDirectoryName(Long dirId, String newName) throws IOException {
        DirectoryEntity directory = (DirectoryEntity)this.directoryRepository.findById(dirId).orElseThrow(() -> new RuntimeException("Directory not found"));
        Path oldDirPath = Paths.get(directory.getPath(), new String[0]);
        Path newDirPath = oldDirPath.getParent().resolve(newName);
        try {
            Files.move(oldDirPath, newDirPath, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
            Stream<Path> paths = Files.walk(newDirPath, new java.nio.file.FileVisitOption[0]);
            try {
                paths.forEach(path -> {
                    Path relativePath = newDirPath.relativize(path);
                    Path updatedPath = newDirPath.resolve(relativePath);
                    // Update directory or file paths in the database
                    if (Files.isDirectory(path, new java.nio.file.LinkOption[0])) {
                        DirectoryEntity subDir = this.directoryRepository.findByPath(path.toString());
                        if (subDir != null) {
                            subDir.setPath(updatedPath.toString());
                            this.directoryRepository.save(subDir);
                        }
                    } else {
                        FileEntity file = this.fileRepository.findByFilepath(path.toString());
                        if (file != null) {
                            file.setFilepath(updatedPath.toString());
                            this.fileRepository.save(file);
                        }
                    }
                });
                if (paths != null)
                    paths.close();
            } catch (Throwable throwable) {
                if (paths != null)
                    try {
                        paths.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (IOException e) {
            throw new IOException("Failed to edit directory name: " + newDirPath.toString(), e);
        }
        directory.setName(newName);
        directory.setPath(newDirPath.toString());
        this.directoryRepository.save(directory);
        log.info("Directory name edited successfully");
    }

    // Creates a new directory within an existing directory specified by pathOfTopDir.
    public DirectoryEntity createDirInDir(String dirName, String pathOfTopDir) throws Exception {
        Path dirPath = Paths.get(pathOfTopDir, new String[] { dirName });
        if (Files.exists(dirPath, new java.nio.file.LinkOption[0])) {
            log.info("Directory already exists: " + dirPath.toString());
            throw new IOException("Directory already exists: " + dirPath.toString());
        }
        Files.createDirectories(dirPath, (FileAttribute<?>[])new FileAttribute[0]);
        log.info("Directory created: " + dirPath.toString());
        DirectoryEntity directory = new DirectoryEntity();
        directory.setName(dirName);
        directory.setPath(dirPath.toString());
        directory.setUser(this.userService.getUserByEmail(((UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()));
        this.directoryRepository.save(directory);
        return directory;
    }

    public DirectoryEntity findDirectoryByName(String name) {
        return this.directoryRepository.findByName(name);
    }

    // Deletes a directory and all its contents (files and subdirectories).
    public void deleteDirectory(Long dirId) {
        DirectoryEntity directory = (DirectoryEntity)this.directoryRepository.findById(dirId).orElseThrow(() -> new RuntimeException("Directory not found"));
        for (FileEntity file : directory.getFiles())
            this.fileService.deleteFile(file.getFilename(), file.getFilepath());
        try {
            Stream<Path> paths = Files.walk(Paths.get(directory.getPath(), new String[0]), new java.nio.file.FileVisitOption[0]);
            try {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (file.isDirectory()) {
                                DirectoryEntity subDir = this.directoryRepository.findByPath(file.getPath());
                                if (subDir != null)
                                    this.directoryRepository.delete(subDir);
                            } else {
                                FileEntity fileEntity = this.fileRepository.findByFilepath(file.getPath());
                                if (fileEntity != null)
                                    this.fileRepository.delete(fileEntity);
                            }
                            if (!file.delete())
                                log.error("Failed to delete file or directory: " + file.getPath());
                        });
                if (paths != null)
                    paths.close();
            } catch (Throwable throwable) {
                if (paths != null)
                    try {
                        paths.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (IOException e) {
            log.error("Failed to delete directory contents: " + directory.getPath(), e);
            throw new RuntimeException("Failed to delete directory contents", e);
        }
        this.directoryRepository.delete(directory);
        log.info("Directory and all its contents deleted successfully: " + directory.getPath());
    }

    // Lists all directories associated with the authenticated user.
    public List<DirectoryEntity> listDirectories(String token) {
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userService.getUserByEmail(userDetails.getUsername());
        try {
            // Walk through the base directory and find directories associated with the user. Max depth is set to 3.
            Stream<Path> paths = Files.walk(Paths.get(this.BASE_DIRECTORY, new String[0]), 3, new java.nio.file.FileVisitOption[0]);
            try {
                List<DirectoryEntity> directories = this.directoryRepository.findByUser(user);
                // Filter the paths to include only directories that belong to the user. Exclude the base directory itself.
                List<DirectoryEntity> list1 = (List)paths.filter(x$0 -> Files.isDirectory(x$0, new java.nio.file.LinkOption[0]))
                        .filter(path -> !path.equals(Paths.get(this.BASE_DIRECTORY)))
                        .map(path -> {
                            String formattedPath = path.toString();
                            return directories.stream()
                                    .filter(dir -> dir.getPath().equals(formattedPath))
                                    .findFirst()
                                    .orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (paths != null)
                    paths.close();
                return list1;
            } catch (Throwable throwable) {
                if (paths != null)
                    try {
                        paths.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to list directories", e);
        }
    }
}
