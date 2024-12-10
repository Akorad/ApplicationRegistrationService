package ru.Darvin.DTO;

import lombok.Data;

@Data
public class IssueByMOLRequest {
    private String molName;                 //МОЛ
    private String comment;                 //Комментарий
    private String nomenclatureCode;        //НоменклатураКод
    private int quantity;                   //Количество
}
