package ru.Darvin.DTO;

import lombok.Data;
import ru.Darvin.Entity.Role;

@Data
public class UserUpdateDto {
    private Role role;
}
