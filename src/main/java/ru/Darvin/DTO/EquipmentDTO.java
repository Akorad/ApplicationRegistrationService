package ru.Darvin.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EquipmentDTO {

    @JsonProperty("ОсновноеСредствоНаименование")
    private String assetName;         // ОсновноеСредствоНаименование

    @JsonProperty("ИнвентарныйНомер")
    private String inventoryNumber;   // ИнвентарныйНомер

    @JsonProperty("ЦМОНаименование")
    private String responsiblePerson; // ЦМОНаименование

    @JsonProperty("СчетУчета")
    private String account;           // СчетУчета

    @JsonProperty("КПС")
    private String kps;               // КПС

    @JsonProperty("КЭК")
    private String kek;               // КЭК

    @JsonProperty("ИФОНаименование")
    private String ifoName;           // ИФОНаименование

    @JsonProperty("КФОСтрока")
    private String kfoLine;           // КФОСтрока

    @JsonProperty("КФОНаим")
    private String kfoName;           // КФОНаим
}
