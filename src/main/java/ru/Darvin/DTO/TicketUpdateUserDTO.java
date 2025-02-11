package ru.Darvin.DTO;

import lombok.Data;

@Data
public class TicketUpdateUserDTO {
    private Long ticketNumber;                  //номер заявки
    private String descriptionOfTheProblem;     //заявленная неисправность
    private String inventoryNumber;             //инвентарный номер
    private String department;                  //Наименование отдела в котором работает
    private String phoneNumber;                 //Номер телефона
    private Boolean refilling;                  //Заправка

}
