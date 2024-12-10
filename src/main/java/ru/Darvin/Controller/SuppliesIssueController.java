package ru.Darvin.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.Darvin.DTO.IssueByInventoryRequest;
import ru.Darvin.DTO.IssueByMOLRequest;
import ru.Darvin.Service.SuppliesIssueService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/SuppliesIssue")
@RequiredArgsConstructor
public class SuppliesIssueController {

    private final SuppliesIssueService suppliesIssueService;

    @PostMapping("/byInventory")
    public ResponseEntity<?> issueByInventory(@RequestBody IssueByInventoryRequest request) {
        suppliesIssueService.issueByInventory(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Материалы выданы по инвентарному номеру.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/byMol")
    public ResponseEntity<?> issueByMol(@RequestBody IssueByMOLRequest request) {
        suppliesIssueService.issueByMol(request);
        return ResponseEntity.ok(Map.of("message", "Материалы выданы по МОЛ."));
    }
}
