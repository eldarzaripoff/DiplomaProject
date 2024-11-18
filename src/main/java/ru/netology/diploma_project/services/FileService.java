package ru.netology.diploma_project.services;

import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma_project.models.entity.File;
import ru.netology.diploma_project.models.entity.User;
import ru.netology.diploma_project.repositories.FileRepository;

import java.io.IOException;

public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void saveFile(MultipartFile file, User user) throws IOException {
        File newFile = new File();
        newFile.setFileName(file.getOriginalFilename());
        newFile.setFileType(file.getContentType());
        newFile.setFileSize(file.getSize());
        newFile.setUser(user);
        newFile.setFileData(file.getBytes());

        fileRepository.save(newFile);
    }
}
