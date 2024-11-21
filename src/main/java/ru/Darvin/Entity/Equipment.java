package ru.Darvin.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Ticket> tickets;     //Ссылка на заявку
}
