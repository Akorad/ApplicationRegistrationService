package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.EquipmentDTO;
import ru.Darvin.Service.EquipmentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipments")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "Поиск по инвентарному номеру", description = "Предоставляет информацию о технике по инвентарному номеру")
    @GetMapping("/{inventoryNumber}")
    public ResponseEntity<EquipmentDTO> getEquipmentByInventoryNumber (@PathVariable String inventoryNumber){

        EquipmentDTO equipmentOptional = equipmentService.findEquipmentByInventoryNumber(inventoryNumber);

        return ResponseEntity.ok(equipmentOptional);
    }

    @Operation(summary = "Список техники", description = "Предоставляет информацию о технике по инвентарному номеру")
    @GetMapping("/summary")
    public Page<EquipmentDTO> getEquipmentByInventoryNumber (@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(required = false) String responsiblePerson){

        return equipmentService.getFilteredEquipmentList(responsiblePerson, page,size);
    }
}
