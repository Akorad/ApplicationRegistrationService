package ru.Darvin.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String firstName;           //Имя
    private String lastName;            //Фамилия
    private String department;          //Наименование отдела в котором работает
    private String phoneNumber;         //Номер телефона
}
