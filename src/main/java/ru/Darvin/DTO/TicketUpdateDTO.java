package ru.Darvin.DTO;

import lombok.Data;
import ru.Darvin.Entity.TicketType;

import java.util.List;

@Data
public class TicketUpdateDTO {
    private Long ticketNumber;                  // ID заявки, которую нужно обновить
    private String detectedProblem;             // Обнаруженная неисправность
    private String comments;                    // Комментарии
    private String typeOfWork;                  // Вид работы
    private TicketType status;                  // Статус заявки
    private List<SuppliesDTO> supplies;         // Материалы

    @Data
    public static class SuppliesDTO {
        private String nomenclatureCode;         // ID номенклатуры
        private Integer quantity;                // Количество материала
    }
}
