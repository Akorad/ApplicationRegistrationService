package ru.Darvin.DTO;

import lombok.Data;

@Data
public class TicketUpdateUserDTO {
    private Long ticketNumber;
    private String descriptionOfTheProblem;
    private String inventoryNumber;
    private String department;          //Наименование отдела в котором работает
    private String phoneNumber;         //Номер телефона
}
