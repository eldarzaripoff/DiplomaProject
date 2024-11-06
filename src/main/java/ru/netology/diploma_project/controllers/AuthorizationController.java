package ru.netology.diploma_project.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.diploma_project.models.requests.AuthorizationRequest;
import ru.netology.diploma_project.models.requests.RegisterRequest;
import ru.netology.diploma_project.models.responses.AuthorizationResponse;
import ru.netology.diploma_project.services.AuthorizationService;

@RestController
@RequiredArgsConstructor
public class AuthorizationController {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);
    private final AuthorizationService authorizationService;

    @PostMapping("/register")
    public ResponseEntity<AuthorizationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authorizationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthorizationResponse> authenticateUser(@RequestBody AuthorizationRequest request) {
        log.info("Получен запрос с логином {} и паролем {}", request.getLogin(), request.getPassword());
        return ResponseEntity.ok(authorizationService.authorization(request));
    }

}
