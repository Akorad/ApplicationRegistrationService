package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.DTO.SupplyUsageDTO;
import ru.Darvin.DTO.TicketWithSuppliesDTO;
import ru.Darvin.Service.SuppliesService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/supplies")
public class SuppliesController {

    @Autowired
    private SuppliesService suppliesService;

    @Operation(summary = "Поиск расходных материалов по номенклатурному коду")
    @GetMapping("/nomenclature/{nomenclatureCode}")
    public ResponseEntity<SuppliesDTO> getSuppliesByNomenclatureCode(@PathVariable String nomenclatureCode){
        SuppliesDTO suppliesDTO = suppliesService.findSuppliesByNomenclatureCode(nomenclatureCode);

        return ResponseEntity.ok(suppliesDTO);
    }

    @Operation(summary = "Получение списка расходных материалов по имени МОЛ")
    @GetMapping("/mol/{mol}")
    public ResponseEntity<List<SuppliesDTO>> getSuppliesGorMOL(@PathVariable String mol){
        List<SuppliesDTO> suppliesList = suppliesService.getSuppliesForMOL(mol);

        return ResponseEntity.ok(suppliesList);
    }

    @Operation(summary = "Получение списка расходных материалов по имени МОЛ")
    @GetMapping("/nomenclatureCode/{nomenclatureCode}")
    public List<SupplyUsageDTO> getTicketsByNomenclatureCode(@PathVariable String nomenclatureCode){
       return suppliesService.getTicketsByNomenclatureCode(nomenclatureCode);
    }

    @Operation(summary = "Получение списка использованных расходных материалов по дате")
    @GetMapping("/filterByDate")
    public ResponseEntity<List<SupplyUsageDTO>> getSuppliesByDate(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        // Получаем список материалов с их использованием в заданном периоде
        List<SupplyUsageDTO> supplies = suppliesService.getSuppliesByDateRange(start, end);
        return ResponseEntity.ok(supplies);
    }
}
