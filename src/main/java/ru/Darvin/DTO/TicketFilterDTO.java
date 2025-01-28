package ru.Darvin.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.Darvin.Entity.TicketType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketFilterDTO {
    private TicketType status;
    private String firstName;
    private String lastName;
    private String editorFirstName;
    private String editorLastName;
    private String inventoryNumber;
    private Boolean hideClosed;  // Новое поле для скрытия заявок с типом CLOSED
    private Boolean hideRefilling;  // Новое поле для скрытия заявок с типом заправка

    public String getFirstNamePattern() {
        return firstName != null ? "%" + firstName + "%" : null;
    }

    public String getLastNamePattern() {
        return lastName != null ? "%" + lastName + "%" : null;
    }

    public String getEditorFirstNamePattern() {
        return editorFirstName != null ? "%" + editorFirstName + "%" : null;
    }

    public String getEditorLastNamePattern() {
        return editorLastName != null ? "%" + editorLastName + "%" : null;
    }
}
