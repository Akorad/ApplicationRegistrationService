package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.Service.SuppliesService;

import java.util.List;

@RestController
@RequestMapping("/supplies")
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
}
