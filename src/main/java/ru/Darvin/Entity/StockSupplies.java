package ru.Darvin.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class StockSupplies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomenclature;        //Номенклатура
    private String nomenclatureCode;    //НоменклатураКод
    private int quantity;               //Количество
    private String mol;                 //МОЛ
    private Boolean includeInReport;    //Отключение отчета прогнозирование
}
