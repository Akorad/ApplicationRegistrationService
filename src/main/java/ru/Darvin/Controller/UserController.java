package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.UserDTO;
import ru.Darvin.DTO.UserInfoDTO;
import ru.Darvin.DTO.UserUpdateDto;
import ru.Darvin.Entity.User;
import ru.Darvin.Service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Метод для получения данных текущего пользователя
    @GetMapping("/me")
    public UserInfoDTO getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
        return userService.getUserByUsername(currentUser.getUsername());
    }

    //Редактирование пользователя
    @Operation(summary = "Редактирование пользователя")
    @PutMapping("/update/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> signIn(@PathVariable String username, @RequestBody UserUpdateDto userUpdateDto) {
        String updateUser = userService.updateUser(username,userUpdateDto );
        return ResponseEntity.ok(updateUser);
    }

    // Метод для получения списка всех пользователей
    @Operation(summary = "Получить всех пользователей")
    @GetMapping("/getAll")
    public List<UserInfoDTO> getAllUsers() {
        return userService.getAllUsers();
    }
}
