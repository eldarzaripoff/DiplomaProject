package ru.netology.diploma_project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.netology.diploma_project.models.entity.File;
import ru.netology.diploma_project.models.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByFileNameAndUser(String fileName, User user);

    void deleteByFileNameAndUser(String fileName, User user);

    public Optional<File> findByFileName(String fileName);

    @Modifying
    @Query("UPDATE File f SET f.fileName = :newFileName WHERE f.fileName = :fileName AND f.user = :user")
    void updateFileNameByUser(String fileName, String newFileName, User user);

    List<File> findAllByUser(User user, Pageable pageable);
}
