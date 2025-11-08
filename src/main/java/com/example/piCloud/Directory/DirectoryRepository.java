package com.example.piCloud.Directory;

import com.example.piCloud.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly=true)
public interface DirectoryRepository extends JpaRepository<DirectoryEntity, Long> {
    DirectoryEntity findByPath(String paramString);

    List<DirectoryEntity> findByUser(User paramUser);

    DirectoryEntity findByName(String paramString);

}
