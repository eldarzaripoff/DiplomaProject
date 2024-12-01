package ru.netology.diploma_project.services;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma_project.controllers.AuthorizationController;
import ru.netology.diploma_project.models.entity.File;
import ru.netology.diploma_project.models.entity.User;
import ru.netology.diploma_project.repositories.FileRepository;
import ru.netology.diploma_project.repositories.TokenRepository;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);

    public FileService(FileRepository fileRepository, TokenRepository tokenRepository, JwtService jwtService) {
        this.fileRepository = fileRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
    }

    @SneakyThrows
    public void saveFile(MultipartFile file, User user, String fileName) {
        File newFile = File.builder()
                .fileName(fileName)
                .fileData(file.getBytes())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .user(user)
                .build();

        fileRepository.save(newFile);
        log.info("Файл сохранён в БД");
    }

    @Transactional
    public ResponseEntity<?> deleteFile(String fileName, User user, String token) {
        var isTokenValid = tokenRepository.findByToken(token)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);
        if (jwtService.isTokenValid(token, user) && isTokenValid) {
            if (!fileRepository.findByFileNameAndUser(fileName, user).isEmpty()) {
                fileRepository.deleteByFileNameAndUser(fileName, user);
                log.info("Файл с именем '{}' удалён для пользователя '{}'", fileName, user.getUsername());
                return ResponseEntity.status(HttpStatus.OK).body("File is deleted successfully");
            } else {
                log.info("Файла с именем '{}' у пользователя '{}' не существует", fileName, user.getUsername());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Такого файла не существует");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please, authorize!");
        }
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

    @SneakyThrows
    public ResponseEntity<String> loadFile(String token, String fileName, User user, MultipartFile file) {
        var isTokenValid = tokenRepository.findByToken(token)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);
        if (jwtService.isTokenValid(token, user) && isTokenValid) {
            if (!file.isEmpty()) {
                if (fileRepository.findByFileName(fileName).isPresent()) {
                    fileName = makeFileNameUnique(fileName);
                }
                saveFile(file, user, fileName);
                log.info("Файл сохранён в БД");
                return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully");
            } else {
                log.info("MultipartFile пуст");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please, attach file");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please, authorize!");
        }
    }

    private String makeFileNameUnique(String fileName) {
        String baseName = fileName;
        String uniqueFileName = fileName;
        int counter = 1;

        while (fileRepository.findByFileName(uniqueFileName).isPresent()) {
            // Проверяем, есть ли уже число в скобках
            String regex = "\\((\\d+)\\)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(uniqueFileName);

            if (matcher.find()) {
                // Если число найдено, увеличиваем его
                int currentNumber = Integer.parseInt(matcher.group(1));
                int newNumber = currentNumber + 1;
                uniqueFileName = uniqueFileName.substring(0, matcher.start(1)) + "" + newNumber + ")";
            } else {
                // Если числа нет, добавляем (1)
                uniqueFileName = baseName + "(" + counter + ")";
            }
            counter++;
        }
        return uniqueFileName;
    }

}
