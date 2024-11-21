package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.*;
import ru.Darvin.Entity.Ticket;
import ru.Darvin.Entity.TicketType;
import ru.Darvin.Service.TicketService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Создать заявку", description = "Создает новую заявку на ремонт")
    @PostMapping("/create")
    public ResponseEntity<Ticket> createTicket(@RequestBody TicketCreateDTO ticketCreateDTO) {
        Ticket createdTicket = ticketService.createTicket(ticketCreateDTO);

        return ResponseEntity.ok(createdTicket);
    }

    @Operation(summary = "Дополняет заявку администратором", description = "Обновляет заявку на ремонт администратором")
    @PutMapping("/update")
    public ResponseEntity<Ticket> updateTicket(@RequestBody TicketUpdateDTO ticketUpdateDTO) {
        Ticket updatedTicket = ticketService.updateTicket(ticketUpdateDTO);

        return ResponseEntity.ok(updatedTicket);
    }

    @Operation(summary = "Обновить заявку пользователя", description = "Обновляет заявку созданной пользователем на ремонт")
    @PutMapping("/userUpdate")
    public ResponseEntity<Ticket> updateTicket(@RequestBody TicketUpdateUserDTO ticketUpdateUserDTO) {
        Ticket updatedUserTicket = ticketService.updateUserTicket(ticketUpdateUserDTO);

        return ResponseEntity.ok(updatedUserTicket);
    }

    @Operation(summary = "Удалить заявку", description = "Удаляет заявку на ремонт")
    @DeleteMapping("/delete/{ticketNumber}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketNumber) {
        ticketService.deleteTicket(ticketNumber);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Информация о заявке", description = "Предоставляет подробную информацию о заявке")
    @GetMapping("/info/{ticketNumber}")
    public ResponseEntity<TicketInfoDTO> getInfoTicket(@PathVariable Long ticketNumber) {
        TicketInfoDTO ticketInfoDTO = ticketService.getTicketInfo(ticketNumber);

        return ResponseEntity.ok(ticketInfoDTO);
    }

    @Operation(summary = "Показать список заявок по фильтру", description = "Предоставляет список заявок, отсортированных по фильтру")
    @GetMapping("/summary")
    public Page<TicketSummaryDTO> getTicketSummaries(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(required = false) TicketType status,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String editorFirstName,
            @RequestParam(required = false) String editorLastName,
            @RequestParam(required = false) String inventoryNumber) {
        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC, "ticketNumber"));
        TicketFilterDTO filter = new TicketFilterDTO(status, firstName, lastName, editorFirstName, editorLastName, inventoryNumber);
        return ticketService.getTicketSummaries(filter, pageable);
    }
}


