package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.DTO.SupplyDTO;
import ru.Darvin.Entity.StockSupplies;
import ru.Darvin.Service.StockSuppliesService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock-supplies")
public class StockSuppliesController {

    private final StockSuppliesService stockSuppliesService;

    @Operation(summary = "Создать или обновляет расходный материал", description = "Создать или обновляет расходный материал")
    @PostMapping("/create")
    public ResponseEntity<StockSupplies> createStockSupplies (@RequestBody SupplyDTO suppliesDTO){
        StockSupplies createStock = stockSuppliesService.createNewStockSupplies(suppliesDTO);
        return ResponseEntity.ok(createStock);
    }

    @Operation(summary = "Удаляет расходный материал", description = "Удаляет расходный материал")
    @DeleteMapping("/delete/{nomenclatureCode}")
    public ResponseEntity<Void> deleteStockSupplies (@PathVariable String nomenclatureCode){
        stockSuppliesService.deleteStockSupplies(nomenclatureCode);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Показывает все расходные материалы", description = "Показывает все расходные материалы")
    @GetMapping("/all")
    public ResponseEntity<List<StockSupplies>> getAllStockSupplies(){
        List<StockSupplies> stockSupplies = stockSuppliesService.getStockSupplies();
        return ResponseEntity.ok(stockSupplies);
    }
}
