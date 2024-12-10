package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.Darvin.DTO.TicketCreateDTO;
import ru.Darvin.DTO.TicketCreateGuestDTO;
import ru.Darvin.Entity.Ticket;
import ru.Darvin.Service.TicketService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guest")
public class GuestController {

    private final TicketService ticketService;

    @Operation(summary = "Создать заявку гостем", description = "Создает новую заявку на ремонт под гостем ")
    @PostMapping("/create")
    public ResponseEntity<Ticket> createTicket(@RequestBody TicketCreateGuestDTO ticketCreateGuestDTO) {
        Ticket createdTicket = ticketService.createGuestTicket(ticketCreateGuestDTO);

        return ResponseEntity.ok(createdTicket);
    }
}
