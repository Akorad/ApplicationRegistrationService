package ru.Darvin.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.Darvin.Entity.TicketType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TicketSummaryDTO {
    private Long ticketNumber;                  //Порядковый номер заявки
    private LocalDateTime createdDate;          //Дата создания
    private LocalDateTime endDate;              //Дата закрытия
    private TicketType status;                  //Статус
    private UserDTO user;                       //ссылка на создавшего заявку пользователя
    private UserDTO editorUser;                 //ссылка на редактирующего заявку пользователя
    private String inventoryNumber;             //Инвентарный номер

    public TicketSummaryDTO(Long ticketNumber, LocalDateTime createdDate, LocalDateTime endDate,
                            TicketType status, UserDTO user, UserDTO editorUser, String inventoryNumber) {
        this.ticketNumber = ticketNumber;
        this.createdDate = createdDate;
        this.endDate = endDate;
        this.status = status;
        this.user = user;
        this.editorUser = editorUser;
        this.inventoryNumber = inventoryNumber;
    }
}
