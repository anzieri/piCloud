package com.example.piCloud.Directory;
import java.util.List;

import com.example.piCloud.File.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Entry point for all directory-related operations such as create, edit, delete, and list directories.
@RestController
@RequestMapping({"/api/v1/directories"})
public class DirectoryController {
    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private FileService fileService;

    // Accepts a directory name as input and uses the DirectoryService to create a new directory.
    @PostMapping({"/create"})
    public ResponseEntity<?> createDirectory(@RequestParam String name) {
        try {
            DirectoryEntity dir = this.directoryService.createDirectory(name);
            return ResponseEntity.ok(dir);
        } catch (Exception e) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Accepts a directory ID and a new name as input, and uses the DirectoryService to update the directory's name.
    @PostMapping({"/editName"})
    public ResponseEntity<?> editFileName(@RequestParam Long dirId, @RequestParam String newName) {
        try {
            this.directoryService.editDirectoryName(dirId, newName);
            return ResponseEntity.ok("File name edited successfully");
        } catch (Exception e) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Accepts a directory ID as input and uses the DirectoryService to delete the specified directory.
    @PostMapping({"/delete"})
    public ResponseEntity<?> deleteDirectory(@RequestParam Long dirId) {
        try {
            this.directoryService.deleteDirectory(dirId);
            return ResponseEntity.ok("Directory deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Retrieves a list of all directories associated with the authenticated user.
    @GetMapping({"/list"})
    public ResponseEntity<List<DirectoryEntity>> listDirectories(@RequestHeader("Authorization") String token) {
        List<DirectoryEntity> directories = this.directoryService.listDirectories(token);
        return ResponseEntity.ok(directories);
    }

    // Accepts a directory name and a parent directory path as input, and uses the DirectoryService to create a new directory within the specified parent directory.
    @PostMapping({"/createDirInDir"})
    public ResponseEntity<?> createDirectoryInDirectory(@RequestParam String name, @RequestParam String parentDirPath) {
        try {
            DirectoryEntity dir = this.directoryService.createDirInDir(name, parentDirPath);
            return ResponseEntity.ok(dir);
        } catch (Exception e) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

