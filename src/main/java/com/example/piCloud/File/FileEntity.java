package com.example.piCloud.File;

import com.example.piCloud.Directory.DirectoryEntity;
import com.example.piCloud.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "file")
public class FileEntity {

    @Id
    @SequenceGenerator(
            name = "file_sequence",
            sequenceName = "file_sequence",
            allocationSize = 1)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "file_sequence")
    @Column(name = "file_id")
    private Long id;

    @Column(name = "file_name", columnDefinition = "TEXT")
    private String filename;

    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filepath;

    @JsonIgnore
    @Column(name = "file_secret_key", columnDefinition = "TEXT")
    private String secretKey;

    // Many-to-one relationship with User
    @ManyToOne
    @JoinColumn(name = "user")
    @JsonIgnore
    private User user;

    // Many-to-one relationship with DirectoryEntity
    @ManyToOne
    @JoinColumn(name = "directory_id")
    @JsonIgnore
    private DirectoryEntity directory;
}
