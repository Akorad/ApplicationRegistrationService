package ru.Darvin.DTO;

import lombok.Data;

@Data
public class IssueByMOLUpdate {
    private Long MOLNumber;                 //Порядковый номер заявки
    private String molName;                 //МОЛ
    private String comment;                 //Комментарий
    private String nomenclatureCode;        //НоменклатураКод
    private int quantity;                   //Количество
}
