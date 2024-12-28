package ru.netology.diploma_project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.diploma_project.models.entity.Role;
import ru.netology.diploma_project.models.entity.User;
import ru.netology.diploma_project.repositories.UserRepository;

@Configuration
@RequiredArgsConstructor
public class DataBaseInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDataBase() {
        return args -> {
            String password = passwordEncoder.encode("aboba");

            if (!userRepository.findByLogin("eldar@mail.ru").isPresent()) {
                User eldar = User.builder()
                        .login("eldar@mail.ru")
                        .password(password)
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(eldar);
            }

            if (!userRepository.findByLogin("rustem").isPresent()) {

                User rustem = User.builder()
                        .login("rustem")
                        .password(password)
                        .role(Role.USER)
                        .build();
                userRepository.save(rustem);
            }
        };
    }
}