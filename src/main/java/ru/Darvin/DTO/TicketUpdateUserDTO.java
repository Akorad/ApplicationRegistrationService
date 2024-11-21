package ru.Darvin.DTO;

import lombok.Data;

@Data
public class TicketUpdateUserDTO {
    private Long ticketNumber;
    private String descriptionOfTheProblem;
    private String inventoryNumber;
}
