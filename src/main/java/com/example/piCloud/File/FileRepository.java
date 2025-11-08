package com.example.piCloud.File;

import com.example.piCloud.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    FileEntity findByFilename(String paramString);

    FileEntity findByFilepath(String paramString);

    List<FileEntity> findByUser(User paramUser);
}
