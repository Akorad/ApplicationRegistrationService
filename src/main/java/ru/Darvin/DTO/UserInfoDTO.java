package ru.Darvin.DTO;

import lombok.Data;
import ru.Darvin.Entity.Role;

@Data
public class UserInfoDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String department;
    private String phoneNumber;
    private Role role;
}
