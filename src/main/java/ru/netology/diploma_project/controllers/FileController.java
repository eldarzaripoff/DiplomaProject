package ru.netology.diploma_project.controllers;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.netology.diploma_project.models.entity.User;
import ru.netology.diploma_project.models.requests.EditNameRequest;
import ru.netology.diploma_project.models.requests.FileUploadRequest;
import ru.netology.diploma_project.repositories.TokenRepository;
import ru.netology.diploma_project.services.FileService;
import ru.netology.diploma_project.services.JwtService;

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
            @ModelAttribute FileUploadRequest fileUploadRequest
    ) {
        log.info("Получен файл с filename: " + fileName);
        return fileService.loadFile(token, fileName, user, fileUploadRequest);
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String fileName,
            @AuthenticationPrincipal User user
    ) {
        log.info("Получен запрос на удаление файла с filename: " + fileName);
        return fileService.dropFile(authToken, fileName, user);
    }

    @GetMapping("/file")
    public ResponseEntity<?> getFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String fileName,
            @AuthenticationPrincipal User user
    ) {
        return fileService.getFile(authToken, fileName, user);
    }

    @PutMapping(value = "/file", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String fileName,
            @AuthenticationPrincipal User user,
            @RequestBody EditNameRequest editNameRequest
    ) {
        return fileService.putFile(authToken, fileName, user, editNameRequest);
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFilesList(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("limit") Integer limit,
            @AuthenticationPrincipal User user
    ) {
        return fileService.getFilesList(authToken, limit, user);
    }

}
