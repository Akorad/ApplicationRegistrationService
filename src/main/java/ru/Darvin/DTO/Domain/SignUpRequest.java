package ru.Darvin.DTO.Domain;

import lombok.Data;
import ru.Darvin.Entity.Role;

@Data
public class SignUpRequest {

    private String username;            //Логин

    private String password;            //Пароль

    private String email;               //Почта

    private String department;          //Наименование отдела в котором работает

    private String phoneNumber;         //Номер телефона

    private String firstName;           //Имя

    private String lastName;            //Фамилия
}
