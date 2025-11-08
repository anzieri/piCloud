package com.example.piCloud.File;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

//Entry point for all file-related operations such as upload, download, delete, and list files.
@RestController
@RequestMapping({"/api/v1/files"})
public class FileController {
    @Autowired
    private FileService fileService;

    @Value("${file.upload-dir}")
    private String BASE_DIRECTORY;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    //Accepts a file and a directory ID as input, and uses the FileService to handle the file upload process.
    @PostMapping({"/upload"})
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam Long directoryId) {
        try {
            this.fileService.uploadFile(file, directoryId);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    //Handles file download requests by accepting a filename and filepath, and returns the requested file as a downloadable resource.
    @PostMapping({"/download"})
    public ResponseEntity<?> downloadFile(@RequestBody FileRequest fileRequest) {
        this.fileService.downloadFile(fileRequest.getFilename(), fileRequest.getFilepath());
        Resource file = this.fileService.downloadFile(fileRequest.getFilename(), fileRequest.getFilepath());
        String encodedFilename = UriUtils.encodePathSegment(fileRequest.getFilename(), StandardCharsets.UTF_8);
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok()
                .header("Content-Disposition", new String[] { "attachment; filename=\"" + encodedFilename + "\"" })).body(file);
    }

    // Handles file deletion requests by accepting a filename and filepath, and uses the FileService to delete the specified file.
    @PostMapping({"/delete"})
    public ResponseEntity<?> deleteFile(@RequestParam String filename, @RequestParam String filepath) {
        try {
            this.fileService.deleteFile(filename, filepath);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Retrieves a list of all files by invoking the FileService and returns the list as a response.
    @GetMapping({"/list"})
    public ResponseEntity<?> listFiles() {
        try {
            return ResponseEntity.ok(this.fileService.listFiles());
        } catch (Exception e) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
