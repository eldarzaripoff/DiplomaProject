package ru.netology.diploma_project.models.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationResponse {
    private String auth_token;

//    public AuthorizationResponse(@JsonProperty("auth-token") String auth_token) {
//        this.auth_token = auth_token;
//    }
}
