package ru.netology.diploma_project.models.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "TOKENS", schema = "netology")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "expired")
    private boolean expired;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType = TokenType.BEARER;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference
    private User user;
}
