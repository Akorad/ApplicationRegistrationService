package ru.Darvin.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.SupplyUsageDTO;
import ru.Darvin.DTO.TicketWithSuppliesDTO;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.DTO.SuppliesList;
import ru.Darvin.Entity.Supplies;
import ru.Darvin.Entity.Ticket;
import ru.Darvin.Exception.EquipmentNotFoundException;
import ru.Darvin.Repository.SuppliesRepository;
import ru.Darvin.Repository.TicketRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuppliesService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String filePath = "src/main/source/stock.json";
    private final SuppliesRepository suppliesRepository;

    public SuppliesService(SuppliesRepository suppliesRepository) {
        this.suppliesRepository = suppliesRepository;
    }

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

    // Получение заявок с расходными материалами
    public List<SupplyUsageDTO> getTicketsByNomenclatureCode(String nomenclatureCode){

        List<Supplies> suppliesList = suppliesRepository.findByNomenclatureCode(nomenclatureCode);

        // Преобразуем записи в DTO
        return suppliesList.stream().map(supply -> {
            SupplyUsageDTO dto = new SupplyUsageDTO();
            dto.setNomenclature(supply.getNomenclature());
            dto.setQuantity(supply.getQuantity());
            dto.setDateOfUse(supply.getDateOfUse());

            // Определяем контекст использования
            if (supply.getTicket() != null) {
                dto.setTicketNumber(supply.getTicket().getTicketNumber());
                dto.setInventoryNumber(supply.getTicket().getEquipment().getInventoryNumber());
            } else if (supply.getMol() != null) {
                dto.setMolName(supply.getSuppliesIssue().getMolName());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // Метод для получения материалов по дате
    public List<SupplyUsageDTO> getSuppliesByDateRange(LocalDate startDate, LocalDate endDate) {
        // Получаем все расходные материалы в заданный период
        List<Supplies> supplies = suppliesRepository.findByDateOfUseBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));
        // Конвертируем данные в DTO
        List<SupplyUsageDTO> supplyUsageDTOs = new ArrayList<>();

        for (Supplies supply : supplies) {
            SupplyUsageDTO dto = new SupplyUsageDTO();
            dto.setNomenclature(supply.getNomenclature());
            dto.setQuantity(supply.getQuantity());
            dto.setDateOfUse(supply.getDateOfUse());

            if (supply.getTicket() != null) {
                // Если материал использовался для ремонта, добавляем номер заявки
                dto.setTicketNumber(supply.getTicket().getTicketNumber());
                dto.setInventoryNumber(supply.getTicket().getEquipment().getInventoryNumber());
                dto.setComments(supply.getTicket().getComments());
            } else if (supply.getSuppliesIssue() != null) {
                // Если материал был выдан по МОЛ, добавляем имя МОЛ
                dto.setMolName(supply.getSuppliesIssue().getMolName());
                dto.setComments(supply.getSuppliesIssue().getComment());
            }

            supplyUsageDTOs.add(dto);
        }

        return supplyUsageDTOs;
    }
}
