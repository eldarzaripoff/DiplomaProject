package ru.netology.diploma_project.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma_project.models.entity.File;
import ru.netology.diploma_project.models.entity.User;
import ru.netology.diploma_project.repositories.TokenRepository;
import ru.netology.diploma_project.services.FileService;
import ru.netology.diploma_project.services.JwtService;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);

    @SneakyThrows
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestHeader(name = "auth-token") String token,
            @RequestParam("filename") String fileName,
            @AuthenticationPrincipal User user,
            @RequestBody MultipartFile file
    ) {
        log.info("Получен файл с filename: " + fileName);
        return fileService.loadFile(token, fileName, user, file);
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

    @GetMapping("/file")
    public ResponseEntity<String> demo() {
        return ResponseEntity.status(HttpStatus.OK).body("Hi!");
    }
}
