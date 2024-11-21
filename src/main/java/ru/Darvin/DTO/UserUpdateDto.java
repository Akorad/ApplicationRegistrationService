package ru.Darvin.DTO;

import lombok.Data;
import ru.Darvin.Entity.Role;

@Data
public class UserUpdateDto {
    private String email;
    private String department;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private Role role;
}
