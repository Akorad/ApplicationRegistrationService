package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.*;
import ru.Darvin.DTO.Mapper.EquipmentMapperImpl;
import ru.Darvin.DTO.Mapper.SuppliesMapperImpl;
import ru.Darvin.DTO.Mapper.TicketMapperImpl;
import ru.Darvin.DTO.Mapper.UserMapperImpl;
import ru.Darvin.Entity.*;
import ru.Darvin.Exception.EquipmentNotFoundException;
import ru.Darvin.Repository.EquipmentRepository;
import ru.Darvin.Repository.TicketRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.Darvin.Entity.Role.ADMIN;
import static ru.Darvin.Entity.Role.GUEST;
import static ru.Darvin.Entity.TicketType.*;

@Service
public class TicketService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SuppliesService suppliesService;

    //формат даты
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    //создание заявки
    public Ticket createTicket(TicketCreateDTO ticketCreateDTO){
        String inventoryNumber = ticketCreateDTO.getEquipment().getInventoryNumber();

        // Проверка наличия заявки на это оборудование
        checkExistingTicket(inventoryNumber);

        // Поиск или создание оборудования
        Equipment equipment = findOrCreateEquipment(inventoryNumber);

        Ticket ticket = new Ticket();

        //Установка порядкового номера
        ticket.setTicketNumber(ticketRepository.findMaxTicketNumber().orElse(0L) + 1);

        //установка полей из ДТО
        ticket.setDescriptionOfTheProblem(ticketCreateDTO.getDescriptionOfTheProblem());
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setStatus(TicketType.CREATED);
        ticket.setEquipment(equipment);

        //Получение текущего пользователя из контекста безопасности
        User user = userService.getCurrentUser();
        ticket.setUser(user);

        return ticketRepository.save(ticket);
    }

    //создание заявки гостем
    public Ticket createGuestTicket(TicketCreateGuestDTO ticketCreateGuestDTO){
        //Получение текущего пользователя из контекста безопасности
        User user = userService.getCurrentUser();
        if (user.getRole().equals(GUEST)) {
            String inventoryNumber = ticketCreateGuestDTO.getEquipment().getInventoryNumber();

            // Проверка наличия заявки на это оборудование
            checkExistingTicket(inventoryNumber);

            // Поиск или создание оборудования
            Equipment equipment = findOrCreateEquipment(inventoryNumber);

            Ticket ticket = new Ticket();

            //Установка порядкового номера
            ticket.setTicketNumber(ticketRepository.findMaxTicketNumber().orElse(0L) + 1);

            //установка полей из ДТО
            ticket.setDescriptionOfTheProblem(ticketCreateGuestDTO.getDescriptionOfTheProblem());
            ticket.setGuestDepartment(ticketCreateGuestDTO.getGuestDepartment());
            ticket.setGuestPhoneNumber(ticketCreateGuestDTO.getGuestPhoneNumber());
            ticket.setCreatedDate(LocalDateTime.now());
            ticket.setStatus(TicketType.CREATED);
            ticket.setEquipment(equipment);

            ticket.setUser(user);

            return ticketRepository.save(ticket);
        } else {
            throw new SecurityException("У вас нет прав для создания такого типа заявки");
        }
    }

    //обновления заявки пользователем
    public Ticket updateUserTicket(TicketUpdateUserDTO ticketUpdateUserDTO){
        Ticket ticket = ticketRepository.findByTicketNumber(ticketUpdateUserDTO.getTicketNumber())
                .orElseThrow(()-> new RuntimeException("Заявка с номером " + ticketUpdateUserDTO.getTicketNumber() + " не найдена"));

        if (ticket.getStatus() != CREATED)
            throw new RuntimeException("Заявка с номером " + ticketUpdateUserDTO.getTicketNumber() + " уже принята и изменить нельзя");


        // Поиск или создание оборудования
        Equipment equipment = findOrCreateEquipment(ticketUpdateUserDTO.getInventoryNumber());

        ticket.setDescriptionOfTheProblem(ticketUpdateUserDTO.getDescriptionOfTheProblem());
        ticket.setEquipment(equipment);

        return ticketRepository.save(ticket);
    }

    //Обновление заявки администратором
    public Ticket updateTicket(TicketUpdateDTO ticketUpdateDTO) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketUpdateDTO.getTicketNumber())
                .orElseThrow(() -> new RuntimeException("Заявка с номером " + ticketUpdateDTO.getTicketNumber() + " не найдена"));

        if (ticket.getStatus() == CLOSED && ticketUpdateDTO.getStatus() != CLOSED) {
            throw new RuntimeException("Заявка с номером " + ticketUpdateDTO.getTicketNumber() + " уже закрыта и её статус изменить нельзя");
        }

        // Установка основных полей заявки
        ticket.setDetectedProblem(ticketUpdateDTO.getDetectedProblem());
        ticket.setComments(ticketUpdateDTO.getComments());
        ticket.setTypeOfWork(ticketUpdateDTO.getTypeOfWork());
        ticket.setStatus(ticketUpdateDTO.getStatus());

        if (ticketUpdateDTO.getStatus().equals(CLOSED) && ticket.getEndDate() == null) {
            ticket.setEndDate(LocalDateTime.now());
        }

        // Суммируем количество материалов из запроса
        Map<String, Integer> incomingSuppliesMap = ticketUpdateDTO.getSupplies().stream()
                .collect(Collectors.toMap(
                        TicketUpdateDTO.SuppliesDTO::getNomenclatureCode,
                        TicketUpdateDTO.SuppliesDTO::getQuantity,
                        Integer::sum
                ));

        // Универсальный метод для обновления материалов
        updateSupplies(ticket, incomingSuppliesMap);

        // Установка редактора заявки
        ticket.setEditorUser(userService.getCurrentUser());

        return ticketRepository.save(ticket);
    }

    //Отображение информации о заявке
    public TicketInfoDTO getTicketInfo(Long ticketNumber){
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(()-> new RuntimeException("Заявка с номером " + ticketNumber + " не найдена"));
        System.out.println(ticket.getGuestDepartment());
        return TicketMapperImpl.INSTANCE.mapToInfoDTO(ticket);
    }

    //Получение списка заявок
    public Page<TicketSummaryDTO> getTicketSummaries(TicketFilterDTO filter, Pageable pageable){
        User summariesUser = userService.getCurrentUser();
        Page<Ticket> tickets;

        if (summariesUser.getRole().equals(ADMIN)) {
            tickets = ticketRepository.findByFilters(
                    filter.getStatus(),
                    filter.getFirstNamePattern(),
                    filter.getLastNamePattern(),
                    filter.getEditorFirstNamePattern(),
                    filter.getEditorLastNamePattern(),
                    filter.getInventoryNumber(),
                    pageable
            );
        } else {
            tickets = ticketRepository.findByUser(summariesUser, pageable);
        }
        return tickets.map(ticket -> new TicketSummaryDTO(
                ticket.getTicketNumber(),
                ticket.getCreatedDate(),
                ticket.getEndDate(),
                ticket.getStatus(),
                UserMapperImpl.INSTANCE.maptoUserDTO(ticket.getUser()),
                ticket.getEditorUser() != null ? UserMapperImpl.INSTANCE.maptoUserDTO(ticket.getEditorUser()) : null,
                ticket.getEquipment().getInventoryNumber(),
                ticket.getGuestDepartment()
        ));
    }

    //Удаление заявки
    public void deleteTicket (Long ticketNumber){
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(()-> new RuntimeException("Заявка с ID " + ticketNumber + " не найдена"));

        //Пользователь удаляющий заявку
        User currentUser  = userService.getCurrentUser();
        if (ticket.getUser().equals(currentUser) || currentUser.getRole() == ADMIN) {
            ticketRepository.delete(ticket);
        } else {
            throw new SecurityException("У вас нет прав для удаления этой заявки");
        }
    }

    //Отправка уведомлений на почту о просроченных заявках
    //@Scheduled(cron = "0 0 10 * * ?")
    public void sendOverdueTicketNotification(){
        List<Ticket> overdueTicket = findOverdueTickets();
        if (!overdueTicket.isEmpty()){
            List<User> admins = userService.findAdmins();

            StringBuilder message  = new StringBuilder("Напоминание о просроченных заявках: \n\n");

            for (Ticket ticket: overdueTicket){
                message.append("Заявка №").append(ticket.getTicketNumber()).append("\n")
                        .append("Дата создания: ").append(ticket.getCreatedDate().format(formatter)).append("\n")
                        .append("Неисправность: ").append(ticket.getDescriptionOfTheProblem()).append("\n")
                        .append("Заявитель: ").append(ticket.getUser().getFirstName())
                        .append(" ").append(ticket.getUser().getLastName()).append("\n");
            }
            for (User admin : admins) {
                String email = admin.getEmail();
                if (email != null && !email.trim().isEmpty()) {
                    emailService.sendEmail(email, "Просроченные заявки", message.toString());
                } else {
                    System.out.println("Отсутствует email для администратора: " + admin.getFirstName() + " " + admin.getLastName());
                }
            }
        }
    }

    //проверка статуса заявки по инвентарному номеру
    private void checkExistingTicket(String inventoryNumber) {
        ticketRepository.findByEquipmentInventoryNumber(inventoryNumber).ifPresent(ticket -> {
            if (ticket.getStatus() != CLOSED) {
                throw new RuntimeException("Заявка с инвентарным номером " + inventoryNumber + " уже находится в работе");
            }
        });
    }

    //поиск техники в базе по инвентарному номеру
    public Equipment findOrCreateEquipment(String inventoryNumber) {
        return equipmentRepository.findByInventoryNumber(inventoryNumber)
                .orElseGet(() -> {
                    EquipmentDTO equipmentDTO = equipmentService.findEquipmentByInventoryNumber(inventoryNumber);
                    if (equipmentDTO == null) {
                        throw new RuntimeException("Техника с инвентарным номером "+ inventoryNumber +" не найдена");
                    }
                    Equipment newEquipment = EquipmentMapperImpl.INSTANCE.mapToEntity(equipmentDTO);
                    return equipmentRepository.save(newEquipment);
                });
    }

    //поиск просроченных заявок
    public List<Ticket> findOverdueTickets(){
        LocalDateTime threeDaysAgo  = LocalDateTime.now().minusDays(3);
        return ticketRepository.findByEndDateBeforeAndStatusNotIn(
                threeDaysAgo,
                List.of(CREATED,IN_WORK)
        );
    }

    //Обновление материалов
    public void updateSupplies(Ticket ticket, Map<String, Integer> incomingSuppliesMap) {
        List<Supplies> suppliesToRemove = new ArrayList<>();

        for (Supplies existingSupply : ticket.getSupplies()) {
            String nomenclatureCode = existingSupply.getNomenclatureCode();

            if (incomingSuppliesMap.containsKey(nomenclatureCode)) {
                int newQuantity = incomingSuppliesMap.get(nomenclatureCode);
                if (existingSupply.getQuantity() != newQuantity) {
                    existingSupply.setQuantity(newQuantity);
                    existingSupply.setDateOfUse(LocalDateTime.now());
                }
                incomingSuppliesMap.remove(nomenclatureCode);
            } else {
                suppliesToRemove.add(existingSupply);
            }
        }

        ticket.getSupplies().removeAll(suppliesToRemove);

        for (Map.Entry<String, Integer> entry : incomingSuppliesMap.entrySet()) {
            String nomenclatureCode = entry.getKey();
            int quantity = entry.getValue();

            SuppliesDTO foundSupply = suppliesService.getSupplies(nomenclatureCode);
            if (foundSupply != null) {
                Supplies newSupply = SuppliesMapperImpl.INSTANCE.mapToSupplies(foundSupply);
                newSupply.setQuantity(quantity);
                newSupply.setTicket(ticket);
                newSupply.setDateOfUse(LocalDateTime.now());
                ticket.getSupplies().add(newSupply);
            } else {
                throw new EquipmentNotFoundException("Материалы с номенклатурным кодом " + nomenclatureCode + " не найдены");
            }
        }
    }

}
