package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.Domain.SignInRequest;
import ru.Darvin.DTO.Domain.SignUpRequest;
import ru.Darvin.DTO.Domain.TokenResponse;
import ru.Darvin.Service.AuthenticationService;

@RestController
@RequiredArgsConstructor
public class SecurityController {

    private final AuthenticationService authenticationService;

    //Регистрация пользователя
    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/auth/sign-up")
    public TokenResponse signUp(@RequestBody SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    //Авторизация пользователя
    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/auth/sign-in")
    public TokenResponse signIn(@RequestBody SignInRequest request) {
        return authenticationService.signIn(request);
    }


}
