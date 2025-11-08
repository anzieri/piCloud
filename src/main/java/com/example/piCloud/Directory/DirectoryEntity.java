package com.example.piCloud.Directory;

import com.example.piCloud.File.FileEntity;
import com.example.piCloud.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Table(name = "directory")
public class DirectoryEntity {

    @Id
    @SequenceGenerator(
            name = "directory_sequence",
            sequenceName = "directory_sequence",
            allocationSize = 1)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "directory_sequence")
    private Long dirId;

    @Column(name = "directory_name", columnDefinition = "TEXT")
    private String name;

    @Column(name = "directory_path", columnDefinition = "TEXT")
    private String path;

    @ManyToOne
    @JoinColumn(name = "user")
    @JsonIgnore
    private User user;

    // One-to-many relationship with FileEntity
    @OneToMany(mappedBy = "directory", cascade = {CascadeType.ALL})
    private List<FileEntity> files;
}
