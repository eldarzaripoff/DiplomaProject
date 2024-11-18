package ru.netology.diploma_project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.diploma_project.models.entity.File;

public interface FileRepository extends JpaRepository<File, Long> {
}
