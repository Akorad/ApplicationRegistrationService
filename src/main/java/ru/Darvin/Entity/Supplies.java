package ru.Darvin.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Supplies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomenclature;        //Номенклатура
    private String nomenclatureCode;    //НоменклатураКод
    private int quantity;               //Количество
    private String mol;                 //МОЛ

    private LocalDateTime dateOfUse;    //дата использования материала

    private Boolean includeInReport;    //Отключение отчета прогнозирование

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @JsonIgnore
    private Ticket ticket;              // Ссылка на заявку

    @ManyToOne
    @JoinColumn(name = "supplies_issue_id")
    @JsonIgnore
    private SuppliesIssue suppliesIssue; // Ссылка на запись выдачи
}
