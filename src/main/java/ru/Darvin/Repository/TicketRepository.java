package ru.Darvin.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.Darvin.Entity.Ticket;
import ru.Darvin.Entity.TicketType;
import ru.Darvin.Entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    Optional<Ticket> findByEquipmentInventoryNumberAndStatusNot(String inventoryNumber, TicketType status);

    @Query("SELECT MAX(t.ticketNumber) FROM Ticket t")
    Optional<Long> findMaxTicketNumber();

    Page<Ticket> findByUser(User user, Pageable pageable);

    Optional<Ticket> findByTicketNumber(Long ticketNumber);

    @Query("SELECT t FROM Ticket t LEFT JOIN t.user u LEFT JOIN t.editorUser e " +
            "WHERE (:status IS NULL OR t.status = :status) " +
            "AND (:firstNamePattern IS NULL OR u.firstName LIKE :firstNamePattern) " +
            "AND (:lastNamePattern IS NULL OR u.lastName LIKE :lastNamePattern) " +
            "AND (:editorFirstNamePattern IS NULL OR e.firstName LIKE :editorFirstNamePattern) " +
            "AND (:editorLastNamePattern IS NULL OR e.lastName LIKE :editorLastNamePattern) " +
            "AND (:inventoryNumber IS NULL OR t.equipment.inventoryNumber = :inventoryNumber) " +
            "AND (:hideClosed IS NULL OR :hideClosed = false OR t.status <> 'CLOSED') " +  // Новое условие
            "AND (:hideRefilling IS NULL OR :hideRefilling = false OR t.refilling IS NULL OR t.refilling = false)")  // Исправленное условие
    Page<Ticket> findByFilters(TicketType status, String firstNamePattern, String lastNamePattern,
                               String editorFirstNamePattern, String editorLastNamePattern,
                               String inventoryNumber, Boolean hideClosed, Boolean hideRefilling,  // Новые параметры
                               Pageable pageable);

    List<Ticket> findByEndDateBeforeAndStatusNotIn(LocalDateTime endDate, List<TicketType> statuses);

    List<Ticket> findByEquipmentInventoryNumberOrderByCreatedDateDesc(String inventoryNumber);

}
