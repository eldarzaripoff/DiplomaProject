package ru.netology.diploma_project.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.netology.diploma_project.models.entity.Token;
import ru.netology.diploma_project.models.entity.TokenType;
import ru.netology.diploma_project.models.requests.AuthorizationRequest;
import ru.netology.diploma_project.models.responses.AuthorizationResponse;
import ru.netology.diploma_project.repositories.TokenRepository;
import ru.netology.diploma_project.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public AuthorizationResponse authorization(AuthorizationRequest authorizationRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authorizationRequest.getLogin(), authorizationRequest.getPassword()));
        var user = userRepository.findByLogin(authorizationRequest.getLogin())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        var allUserValidTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (!allUserValidTokens.isEmpty()) {
            allUserValidTokens.forEach(t -> {
                t.setExpired(true);
                t.setRevoked(true);
            });
            tokenRepository.saveAll(allUserValidTokens);
        }
        tokenRepository.save(token);
        return new AuthorizationResponse().setAuthToken(jwtToken);
    }
}
