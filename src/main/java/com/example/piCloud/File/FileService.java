package com.example.piCloud.File;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.example.piCloud.Directory.DirectoryEntity;
import com.example.piCloud.Directory.DirectoryRepository;
import com.example.piCloud.User.User;
import com.example.piCloud.User.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

@Slf4j
@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private UserService userService;

    private static final String ALGORITHM = "AES";

    private static final int KEY_SIZE = 256;

    private static final String TRANSFORMATION = "AES";


    /** Lists all files associated with the currently authenticated user
     * by retrieving the user details from the security context and querying the file repository. */
    public List<FileEntity> listFiles() {
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userService.getUserByEmail(userDetails.getUsername());
        log.info("Listing files for user: " + user.getEmail());
        return this.fileRepository.findByUser(user);
    }

    /** Downloads and decrypts a file given its filename and filepath.
     * It reads the encrypted file from the filesystem, retrieves the associated secret key from the database,
     * decrypts the file content, and returns it as a Resource. */
    public Resource downloadFile(String filename, String filepath) {
        Path filePath = Paths.get(filepath, new String[0]);
        if (Files.exists(filePath, new java.nio.file.LinkOption[0]))
            try {
                byte[] encryptedData = Files.readAllBytes(filePath);
                FileEntity fileEntity = this.fileRepository.findByFilename(filename);
                byte[] decodedKey = Base64.getDecoder().decode(fileEntity.getSecretKey());
                SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                byte[] decryptedData = decryptFile(Base64.getEncoder().encodeToString(encryptedData), secretKey);
                return (Resource)new ByteArrayResource(decryptedData);
            } catch (AccessDeniedException e) {
                log.error("Access denied to file: " + filename, e);
                throw new FileDownloadException(this, "Access denied to file: " + filename, e);
            } catch (IOException e) {
                log.error("Failed to download file: " + filename, e);
                throw new FileDownloadException(this, "Failed to download file: " + filename, e);
            } catch (Exception e) {
                log.error("Failed to decrypt file: " + filename, e);
                throw new FileDownloadException(this, "Failed to decrypt file: " + filename, e);
            }
        log.error("File not found: " + filepath);
        throw new FileNotFoundException(this, "File not found: " + filepath);
    }

    /** Encrypts the content of a MultipartFile using AES encryption with the provided SecretKey.
     * Returns the encrypted data as a Base64-encoded string. */
    public String encryptFile(MultipartFile file, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(1, secretKey);
        byte[] encryptedBytes = cipher.doFinal(file.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /** Decrypts a Base64-encoded string of encrypted data using the provided SecretKey.
     * Returns the decrypted data as a byte array. */
    public byte[] decryptFile(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(2, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        return cipher.doFinal(decodedBytes);
    }

    /** Generates a new AES SecretKey with a key size of 256 bits. */
    public SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    /** Uploads a MultipartFile to a specified directory by encrypting its content,
     * saving it to the filesystem, and storing its metadata and secret key in the database. */
    public void uploadFile(MultipartFile file, Long directoryId) throws Exception {
        DirectoryEntity directory = (DirectoryEntity)this.directoryRepository.findById(directoryId).orElseThrow(() -> new FileNotFoundException(this, "Directory not found"));
        Metadata metadata = extractMetadata(file);
        SecretKey secretKey = generateSecretKey();
        String encryptedData = encryptFile(file, secretKey);
        Path storageLocation = Paths.get(directory.getPath(), new String[0]);
        Files.write(storageLocation.resolve(file.getOriginalFilename()), Base64.getDecoder().decode(encryptedData), new java.nio.file.OpenOption[0]);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFilename(file.getOriginalFilename());
        fileEntity.setFilepath(storageLocation.toString());
        fileEntity.setDirectory(directory);
        fileEntity.setSecretKey(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        log.info("Content type: " + metadata.get("Content-Type"));
        log.info("Content size: " + metadata.get("Content-Length"));
        this.fileRepository.save(fileEntity);
    }

    /** Extracts metadata from a MultipartFile using Apache Tika.
     * Returns the extracted Metadata object. */
    private Metadata extractMetadata(MultipartFile file) throws IOException {
        try {
            InputStream stream = file.getInputStream();
            try {
                Metadata metadata = new Metadata();
                Parser parser = (new Tika()).getParser();
                ToHTMLContentHandler toHTMLContentHandler = new ToHTMLContentHandler();
                ParseContext context = new ParseContext();
                parser.parse(stream, (ContentHandler)toHTMLContentHandler, metadata, context);
                Metadata metadata1 = metadata;
                if (stream != null)
                    stream.close();
                return metadata1;
            } catch (Throwable throwable) {
                if (stream != null)
                    try {
                        stream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (SAXException|org.apache.tika.exception.TikaException e) {
            log.error("Failed to extract metadata", e);
            throw new IOException("Failed to extract metadata", e);
        }
    }

    /** Deletes a file given its filename and filepath.
     * It removes the file from the filesystem and deletes its corresponding record from the database. */
    public void deleteFile(String filename, String filepath) {
        Path filePath = Paths.get(filepath, new String[] { filename });
        try {
            Files.deleteIfExists(filePath);
            FileEntity fileEntity = this.fileRepository.findByFilename(filename);
            this.fileRepository.delete(fileEntity);
            log.info("File deleted successfully: " + filename);
        } catch (IOException e) {
            log.error("Failed to delete file: " + filename, e);
            throw new FileDownloadException(this, "Failed to delete file: " + filename, e);
        }
    }
}
