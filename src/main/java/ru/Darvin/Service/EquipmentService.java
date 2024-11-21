package ru.Darvin.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.EquipmentDTO;
import ru.Darvin.DTO.EquipmentList;
import ru.Darvin.Exception.EquipmentNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class EquipmentService {


    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String filePath = "src/main/source/inventoryNumbers.json";


    //Поиск техники по инвентарному номеру
    public EquipmentDTO findEquipmentByInventoryNumber (String inventoryNumber){
        try {
            File jsonFile = new File(filePath);
            EquipmentList equipmentListWrapper = objectMapper.readValue(jsonFile, EquipmentList.class);

            return equipmentListWrapper.getEquipmentList().stream()
                    .filter(e -> inventoryNumber.equals(e.getInventoryNumber()))
                    .findFirst()
                    .orElseThrow(() -> new EquipmentNotFoundException("Оборудование с инвентарным номером " + inventoryNumber + " не найдено"));

        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Вывод списка техники с пагинацией
    public Page<EquipmentDTO> getFilteredEquipmentList (String responsiblePerson, int page, int size){
        try {
            File jsonFile = new File(filePath);
            EquipmentList equipmentListWrapper = objectMapper.readValue(jsonFile, EquipmentList.class);

            List<EquipmentDTO> filteredEquipmentList = equipmentListWrapper.getEquipmentList().stream()
                    .filter(e -> responsiblePerson == null || e.getResponsiblePerson().toLowerCase().contains(responsiblePerson.toLowerCase()))
                    .toList();

            Pageable pageable = PageRequest.of(page,size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()),filteredEquipmentList.size());
            List<EquipmentDTO> paginatedList = filteredEquipmentList.subList(start,end);

            return new PageImpl<>(paginatedList, pageable,filteredEquipmentList.size());

        } catch (IOException e) {
            e.printStackTrace();
            return Page.empty();
        }
    }
}
