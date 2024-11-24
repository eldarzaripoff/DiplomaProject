package ru.netology.diploma_project.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma_project.models.entity.File;
import ru.netology.diploma_project.models.entity.User;
import ru.netology.diploma_project.services.FileService;
import ru.netology.diploma_project.services.JwtService;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);

    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String fileName,
            @RequestParam("file") @Valid MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        try {
            if (!jwtService.isTokenValid(authToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid token");
            }

            log.info("Запрос прошёл аутентификацию");
            fileService.saveFile(file, user, fileName);
            log.info("Файл сохранён в БД");
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error in file upload: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String fileName,
            @AuthenticationPrincipal User user
    ) {
        try {
            if (!jwtService.isTokenValid(authToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid token");
            }

            log.info("Запрос прошёл аутентификацию");
            Optional<File> foundFile = fileService.findFileByNameAndUser(fileName, user);
            if (foundFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Такого файла не существует");
            } else {
                fileService.deleteFile(fileName, user);
                return ResponseEntity.status(HttpStatus.OK).body("File deleted successfully");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
