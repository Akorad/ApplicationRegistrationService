package ru.Darvin.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketWithSuppliesDTO {
    private long ticketNumber;                    //Номер заявки
    private LocalDateTime dateOfUse;              //Дата использования
    private int quantity;                         //Количество использованных
}
