package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.IssueByInventoryRequest;
import ru.Darvin.DTO.IssueByMOLRequest;
import ru.Darvin.DTO.Mapper.SuppliesMapperImpl;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.Entity.*;
import ru.Darvin.Exception.EquipmentNotFoundException;
import ru.Darvin.Exception.TicketProcessingException;
import ru.Darvin.Repository.IssueRepository;
import ru.Darvin.Repository.SuppliesRepository;
import ru.Darvin.Repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.Darvin.Entity.TicketType.CLOSED;

@Service
public class SuppliesIssueService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SuppliesRepository suppliesRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SuppliesService suppliesService;

    @Autowired
    private UserService userService;

    @Autowired
    private IssueRepository issueRepository;

    // Метод выдачи по инвентарному номеру
    public Ticket issueByInventory(IssueByInventoryRequest request) {
        // Проверяем, существует ли оборудование
        Equipment equipment = ticketService.findOrCreateEquipment(request.getInventoryNumber());

        // Ищем существующую заявку по инвентарному номеру, где статус НЕ CLOSED
        Ticket ticket = ticketRepository.findByEquipmentInventoryNumberAndStatusNot(request.getInventoryNumber(), TicketType.CLOSED)
                .orElseGet(() -> createNewTicket(request, equipment));

        // Поиск расходного материала по номенклатурному коду
        SuppliesDTO foundSupply = suppliesService.getSupplies(request.getNomenclatureCode());
        if (foundSupply == null) {
            throw new EquipmentNotFoundException("Материалы с номенклатурным кодом " + request.getNomenclatureCode() + " не найдены");
        }

        // Создание объекта Supplies из найденного материала
        Supplies supplies = SuppliesMapperImpl.INSTANCE.mapToSupplies(foundSupply);
        supplies.setQuantity(request.getQuantity());
        supplies.setTicket(ticket); // Привязываем материал к текущей заявке
        supplies.setDateOfUse(LocalDateTime.now());

        // Добавляем материал в заявку
        ticket.getSupplies().add(supplies);

        ticket.setEditorUser(userService.getCurrentUser());
        return ticketRepository.save(ticket);
    }



    // Метод выдачи по МОЛ
    public void issueByMol(IssueByMOLRequest request) {

        // Создание нового объекта SuppliesIssue
        SuppliesIssue suppliesIssue = new SuppliesIssue();
        suppliesIssue.setMolName(request.getMolName());
        suppliesIssue.setComment(request.getComment());
        suppliesIssue.setUser(userService.getCurrentUser());
        suppliesIssue.setIssueDate(LocalDateTime.now());

        // Инициализация списка supplies, если он не был инициализирован
        if (suppliesIssue.getSupplies() == null) {
            suppliesIssue.setSupplies(new ArrayList<>());
        }

        // Поиск расходного материала по номенклатурному коду
        SuppliesDTO foundSupply = suppliesService.getSupplies(request.getNomenclatureCode());

        // Проверка, если материал не найден
        if (foundSupply == null) {
            throw new EquipmentNotFoundException("Материалы с номенклатурным кодом " + request.getNomenclatureCode() + " не найдены");
        }

        // Создание объекта Supplies из найденного материала
        Supplies supplies = SuppliesMapperImpl.INSTANCE.mapToSupplies(foundSupply);
        supplies.setQuantity(request.getQuantity());
        supplies.setSuppliesIssue(suppliesIssue);
        supplies.setDateOfUse(LocalDateTime.now());

        // Добавление материала в список
        suppliesIssue.getSupplies().add(supplies);

        // Сохранение записи о выдаче в репозиторий
        issueRepository.save(suppliesIssue);
    }

    // Метод создания заявки по инвентарному
    private Ticket createNewTicket(IssueByInventoryRequest request, Equipment equipment) {
        Ticket ticket = new Ticket();
        ticket.setDescriptionOfTheProblem("автоматически созданная заявка на выдачу со склада");
        ticket.setEquipment(equipment);
        ticket.setUser(userService.getCurrentUser());
        ticket.setStatus(TicketType.CREATED);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setTicketNumber(ticketRepository.findMaxTicketNumber().orElse(0L) + 1);
        ticket.setComments(request.getComment());
        ticket.setSupplies(new ArrayList<>());
        return ticketRepository.save(ticket);
    }
}

