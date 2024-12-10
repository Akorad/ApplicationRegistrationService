package ru.Darvin.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketPrintDTO {
    private Long ticketNumber;              // Порядковый номер заявки
    private LocalDateTime createdDate;      // Дата создания
    private LocalDateTime endDate;          // Дата закрытия
    private String descriptionOfTheProblem; // Заявленная неисправность
    private String detectedProblem;         // Обнаруженная неисправность
    private String comments;                // Комментарии
    private String typeOfWork;              // Вид работы
    private String status;                  // Статус заявки
    private String equipmentName;           // Название оборудования
    private String inventoryNumber;         // Инвентарный номер оборудования
private UserDTO user;                       //ссылка на создавшего заявку пользователя
private UserDTO editorUser;                 //ссылка на редактирующего заявку пользователя
    private List<SupplyDTO> supplies;       // Список расходных материалов
}

