package ru.Darvin.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SuppliesList {
    @JsonProperty("МатериальныеЗапасы")
    private List<SuppliesDTO> suppliesList;
}
