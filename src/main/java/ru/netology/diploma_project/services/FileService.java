package ru.netology.diploma_project.services;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma_project.controllers.AuthorizationController;
import ru.netology.diploma_project.models.entity.File;
import ru.netology.diploma_project.models.entity.User;
import ru.netology.diploma_project.models.entity.errors.DeleteFileError;
import ru.netology.diploma_project.models.entity.errors.InputDataError;
import ru.netology.diploma_project.models.entity.errors.UnauthorizedError;
import ru.netology.diploma_project.models.entity.errors.UploadFileError;
import ru.netology.diploma_project.models.requests.EditNameRequest;
import ru.netology.diploma_project.models.requests.FileUploadRequest;
import ru.netology.diploma_project.models.responses.FileResponse;
import ru.netology.diploma_project.models.responses.ListOfFilesResponse;
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
    public void saveFile(MultipartFile file, User user, String fileName, String hash) {
        File newFile = File.builder()
                .fileName(fileName)
                .fileData(file.getBytes())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .user(user)
                .hash(hash)
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
            log.info("Запрос прошёл аутентификацию успешно");
            if (!fileRepository.findByFileNameAndUser(fileName, user).isEmpty()) {
                log.info("Файл с именем '{}' пользователя '{}' существует", fileName, user.getUsername());
                try {
                    fileRepository.deleteByFileNameAndUser(fileName, user);
                    log.info("Файл с именем '{}' удалён для пользователя '{}'", fileName, user.getUsername());
                    return ResponseEntity.status(HttpStatus.OK).body("File is deleted successfully");
                } catch (Exception e) {
                    log.error("Ошибка при удалении файла: ", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при удалении файла");
                }
            } else {
                log.info("Файла с именем '{}' у пользователя '{}' не существует", fileName, user.getUsername());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InputDataError());
            }
        } else {
            log.warn("Аутентификация не пройдена");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UnauthorizedError());
        }
    }

    public Optional<File> findFileByNameAndUser(String fileName, User user) {
        Optional<File> file = fileRepository.findByFileNameAndUser(fileName, user);
        if (file.isEmpty()) {
            log.info("Файл с именем '{}' не найден для пользователя '{}'", fileName, user.getUsername());
            return Optional.empty();
        }
        log.info("Файл с именем '{}' найден для пользователя '{}'", fileName, user.getUsername());
        return file; // Возвращаем первый найденный файл
    }

    @SneakyThrows
    public ResponseEntity<?> loadFile(String token, String fileName, User user, FileUploadRequest fileUploadRequest) {
        var isTokenValid = tokenRepository.findByToken(token)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);
        if (jwtService.isTokenValid(token, user) && isTokenValid) {
            String hash = fileUploadRequest.getHash();
            MultipartFile file = fileUploadRequest.getFile();
            if (!file.isEmpty()) {
                if (fileRepository.findByFileName(fileName).isPresent()) {
                    fileName = makeFileNameUnique(fileName);
                }
                saveFile(file, user, fileName, hash);
                log.info("Файл сохранён в БД");
                return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully");
            } else {
                log.info("MultipartFile пуст");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InputDataError());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UnauthorizedError());
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

    @Transactional
    public ResponseEntity<?> dropFile(String authToken, String fileName, User user) {
        try {
            Optional<File> foundFile = findFileByNameAndUser(fileName, user);
            if (foundFile.isEmpty()) {
                log.warn("Такого файла не существует");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InputDataError());
            } else {
                deleteFile(fileName, user, authToken);
                return ResponseEntity.status(HttpStatus.OK).body("File deleted successfully");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DeleteFileError());
        }
    }

    public ResponseEntity<?> getFile(String authToken, String fileName, User user) {
        try {
            if (!jwtService.isTokenValid(authToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UnauthorizedError());
            }
            log.info("Запрос прошёл аутентификацию");
            Optional<File> foundFile = findFileByNameAndUser(fileName, user);
            if (foundFile.isEmpty()) {
                log.info("Файла не существует");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InputDataError());
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(foundFile.get());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UploadFileError());
        }
    }

    @Transactional
    public ResponseEntity<?> putFile(String authToken, String fileName, User user, EditNameRequest editNameRequest) {
        try {
            if (!jwtService.isTokenValid(authToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UnauthorizedError());
            }
            log.info("Запрос прошёл аутентификацию");
            Optional<File> foundFile = findFileByNameAndUser(fileName, user);
            if (foundFile.isEmpty()) {
                log.info("Искомый файл не найден");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InputDataError());
            } else if (editNameRequest.getNewName().isBlank() || editNameRequest.getNewName().isEmpty()) {
                log.info("Не введено новое имя файла");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InputDataError());
            } else {
                log.info("Файл '{}' найден", foundFile.get().getFileName());
                log.info("Новое имя файла: '{}'", editNameRequest.getNewName());
                fileRepository.updateFileNameByUser(foundFile.get().getFileName(), editNameRequest.getNewName(), user);
                log.info("Файл '{}' переименован в '{}'", fileName, editNameRequest.getNewName());
                return ResponseEntity.status(HttpStatus.OK).body("File name edited successfully");
            }
        } catch (Exception e) {
            log.error("ошибка при обработке запроса", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UploadFileError());
        }
    }

    public ResponseEntity<?> getFilesList(String authToken, Integer limit, User user) {
        try {
            if (!jwtService.isTokenValid(authToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UnauthorizedError());
            }
            log.info("Запрос прошёл аутентификацию");
            List<File> fileList = getFilesByUser(user, limit);
            ListOfFilesResponse listOfFilesResponse = new ListOfFilesResponse();
            for (File file : fileList) {
                listOfFilesResponse.getList().add(new FileResponse(file.getFileName(), Math.toIntExact(file.getFileSize())));
            }
            return ResponseEntity.status(HttpStatus.OK).body(listOfFilesResponse);

        } catch (Exception e) {
            log.error("ошибка при обработке запроса", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UploadFileError());
        }
    }

    public List<File> getFilesByUser(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return fileRepository.findAllByUser(user, pageable);
    }
}
