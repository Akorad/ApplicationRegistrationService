package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.Service.EmailService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final EmailService emailService;

    @Operation(summary = "Тест почты", description = "Рассылает почту")
    @PostMapping("/test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String email) {
        emailService.sendTestEmail(email);
        return ResponseEntity.ok("Test email sent successfully to " + email);
    }
}
