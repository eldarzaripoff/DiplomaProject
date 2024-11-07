package ru.netology.diploma_project.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.netology.diploma_project.models.requests.AuthorizationRequest;
import ru.netology.diploma_project.models.responses.AuthorizationResponse;
import ru.netology.diploma_project.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthorizationResponse authorization(AuthorizationRequest authorizationRequest) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authorizationRequest.getLogin(),
                authorizationRequest.getPassword()
        ));
        var user = userRepository.findByLogin(authorizationRequest.getLogin())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthorizationResponse.builder().auth_token(jwtToken).build();
    }
}
