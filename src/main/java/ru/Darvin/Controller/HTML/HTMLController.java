package ru.Darvin.Controller.HTML;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.Darvin.DTO.TicketInfoDTO;
import ru.Darvin.Entity.User;
import ru.Darvin.Service.TicketService;
import ru.Darvin.Service.UserService;

@Controller // Измените на Controller
@RequiredArgsConstructor
@RequestMapping("/api/html")
public class HTMLController {

    private final TicketService ticketService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    @Operation(summary = "Информация о заявке", description = "Предоставляет подробную информацию о заявке для модельного окна")
    @GetMapping("/tickets/info/{ticketNumber}")
    public String getTicketInfo(@PathVariable Long ticketNumber, Model model) {
        TicketInfoDTO ticketInfo = ticketService.getTicketInfo(ticketNumber); // Метод получения данных заявки
        model.addAttribute("ticket", ticketInfo);
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        // Преобразование списка supplies в JSON
        try {
            String suppliesJson = objectMapper.writeValueAsString(ticketInfo.getSupplies());
            model.addAttribute("suppliesJson", suppliesJson); // Добавляем JSON в модель
        } catch (Exception e) {
            throw new RuntimeException("Ошибка преобразования списка supplies в JSON", e);
        }

        return "ticket-modal"; // Возвращаем имя шаблона для рендеринга
    }
}
