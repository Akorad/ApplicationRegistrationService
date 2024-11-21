package ru.Darvin.DTO.Domain;

import lombok.Data;

@Data
public class SignInRequest {

    private String username;

    private String password;
}
