package ru.netology.diploma_project.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma_project.controllers.AuthorizationController;
import ru.netology.diploma_project.models.entity.File;
import ru.netology.diploma_project.models.entity.User;
import ru.netology.diploma_project.repositories.FileRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void saveFile(MultipartFile file, User user, String fileName) throws IOException {
        File newFile = new File();
        newFile.setFileName(fileName);
        newFile.setFileType(file.getContentType());
        newFile.setFileSize(file.getSize());
        newFile.setUser(user);
        newFile.setFileData(file.getBytes());

        fileRepository.save(newFile);
        log.info("Файл сохранён в БД");
    }

    public void deleteFile(String fileName, User user) {
        fileRepository.deleteByFileNameAndUser(fileName, user);
        log.info("Файл с именем '{}' удалён для пользователя '{}'", fileName, user.getUsername());
    }

    public Optional<File> findFileByNameAndUser(String fileName, User user) {
        List<File> files = fileRepository.findByFileNameAndUser(fileName, user);
        if (files.isEmpty()) {
            log.info("Файл с именем '{}' не найден для пользователя '{}'", fileName, user.getUsername());
            return Optional.empty();
        }
        log.info("Файл с именем '{}' найден для пользователя '{}'", fileName, user.getUsername());
        return Optional.of(files.get(0)); // Возвращаем первый найденный файл
    }
}
