package ru.Darvin.DTO;

import lombok.Data;
import ru.Darvin.Entity.Supplies;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IssueByMOLHistory {

    private Long MOLNumber;                 //Порядковый номер заявки
    private String molName;                 //МОЛ
    private String comment;                 //Комментарий
    private LocalDateTime issueDate;        // Дата выдачи

    private UserDTO user;                   // Ссылка на пользователя, создавшего запись

    private List<Supplies> supplies;        // Список материалов

}
