package ru.Darvin.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.DTO.SuppliesList;
import ru.Darvin.Exception.EquipmentNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuppliesService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String filePath = "src/main/source/stock.json";

    //Поиск расходных материалов по номенклатурному коду
    public SuppliesDTO findSuppliesByNomenclatureCode (String nomenclatureCode){
        try {
            File jsonFile = new File(filePath);
            SuppliesList suppliesListWrapper = objectMapper.readValue(jsonFile, SuppliesList.class);

            return suppliesListWrapper.getSuppliesList().stream()
                    .filter(supply -> nomenclatureCode.equals(supply.getNomenclatureCode()))
                    .findFirst()
                    .orElseThrow(() -> new EquipmentNotFoundException("Расходные материалы с номенклатурным кодом " + nomenclatureCode + " не найдены"));

        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Метод для получения списка расходных материалов по МОЛ
    public List<SuppliesDTO> getSuppliesForMOL(String malName){
            List<SuppliesDTO> suppliesList = getSupplies();

        System.out.println(malName);
            return suppliesList.stream()
                    .filter(supply -> malName.equals(supply.getMol()))
                    .collect(Collectors.toList());

    }

    //поиск расходного материала
    public SuppliesDTO getSupplies (String nomenclatureCode){
        List<SuppliesDTO> suppliesList = getSupplies();

        return suppliesList.stream()
                .filter(supply -> nomenclatureCode.equals(supply.getNomenclatureCode()))
                .findFirst()
                .orElseThrow(() -> new EquipmentNotFoundException("Расходные материалы с номенклатурным кодом " + nomenclatureCode + " не найдено"));

    }

    //получение списка расходных материалов
    @SneakyThrows
    public List<SuppliesDTO> getSupplies(){
        File jsonFile = new File(filePath);

        SuppliesList suppliesListWrapper = objectMapper.readValue(jsonFile, SuppliesList.class);

        return suppliesListWrapper.getSuppliesList();
    }
}
