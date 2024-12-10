package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.Service.EmailService;
import ru.Darvin.Service.TranslationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final EmailService emailService;
    
    private final TranslationService translationService;

    @Operation(summary = "Тест почты", description = "Рассылает почту")
    @PostMapping("/test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String email) {
        emailService.sendTestEmail(email);
        return ResponseEntity.ok("Test email sent successfully to " + email);
    }

    @Operation(summary = "Тест перевода", description = "Проверяем перевод API")
    @GetMapping("/test-translate")
    public ResponseEntity<String> sendTestTranslate(@RequestParam String translate) {
        String otvet =  translationService.translateToEnglish(translate);
        return ResponseEntity.ok("Перевод: " + otvet);
    }
}
