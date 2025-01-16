package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.*;
import ru.Darvin.Entity.Ticket;
import ru.Darvin.Service.SuppliesIssueService;
import ru.Darvin.Service.SuppliesService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/SuppliesIssue")
@RequiredArgsConstructor
public class SuppliesIssueController {

    private final SuppliesIssueService suppliesIssueService;
    private final SuppliesService suppliesService;

    @PostMapping("/create/byInventory")
    public ResponseEntity<?> issueByInventory(@RequestBody IssueByInventoryRequest request) {
        suppliesIssueService.issueByInventory(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Материалы выданы по инвентарному номеру.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create/byMol")
    public ResponseEntity<?> issueByMol(@RequestBody IssueByMOLRequest request) {
        suppliesIssueService.issueByMol(request);
        return ResponseEntity.ok(Map.of("message", "Материалы выданы по МОЛ."));
    }

    @Operation(summary = "Информация о расходах по МОЛ", description = "Предоставляет подробную информацию о списаниях по МОЛ")
    @GetMapping("/history")
    public ResponseEntity<List<IssueByMOLHistory>> getHistoryIssue() {
        List <IssueByMOLHistory> issueByMOLHistory = suppliesIssueService.getHistoryIssue();

        return ResponseEntity.ok(issueByMOLHistory);
    }

    @Operation(summary = "Обновление информации о расходах по МОЛ", description = "Предоставляет возможность обновить информацию о списаниях по МОЛ")
    @PutMapping("/update")
    public ResponseEntity<IssueByMOLHistory> updateIssue(@RequestBody IssueByMOLUpdate request) {
        IssueByMOLHistory updatedUserTicket = suppliesIssueService.updateIssue(request);

        return ResponseEntity.ok(updatedUserTicket);
    }

    @Operation(summary = "Удаление информации о расходах по МОЛ", description = "Предоставляет возможность удалить информацию о списаниях по МОЛ")
    @DeleteMapping("/delete/{MOLNumber}")
    public ResponseEntity<?> deleteIssue(@PathVariable Long MOLNumber) {
        suppliesIssueService.deleteIssue(MOLNumber);

        return ResponseEntity.ok(Map.of("message", "Заявка о выдаче удалена."));
    }
}
