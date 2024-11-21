package ru.Darvin.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SuppliesDTO {

    @JsonProperty("Номенклатура")
    private String nomenclature;        //Номенклатура

    @JsonProperty("НоменклатураКод")
    private String nomenclatureCode;    //НоменклатураКод

    @JsonProperty("Количество")
    private int quantity;               //Количество

    @JsonProperty("МОЛ")
    private String mol;                 //МОЛ
}
