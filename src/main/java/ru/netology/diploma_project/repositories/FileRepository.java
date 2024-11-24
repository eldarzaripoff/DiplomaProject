package ru.netology.diploma_project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.diploma_project.models.entity.File;
import ru.netology.diploma_project.models.entity.User;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByFileNameAndUser(String fileName, User user);
    void deleteByFileNameAndUser(String fileName, User user);
}
