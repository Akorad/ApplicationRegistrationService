package ru.Darvin.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tickets")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;                            // id

    @Column(name = "ticket_number", unique = true, nullable = false)
    private Long ticketNumber;                  //Порядковый номер заявки

    private LocalDateTime createdDate;          //Дата создания

    @Column(nullable = true)
    private LocalDateTime readyDate;            //Дата готовности

    @Column(nullable = true)
    private LocalDateTime endDate;              //Дата закрытия

    @Column(nullable = true)
    private Boolean refilling;                  //Заправка

    @Column(length = 1000)
    private String descriptionOfTheProblem;     //Заявленная неисправность

    @Column(nullable = true,length = 1000)
    private String detectedProblem;             //Обнаруженная неисправность

    @Column(nullable = true,length = 1000)
    private String comments;                    //Комментарии

    @Column(nullable = true)
    private String typeOfWork;                  //Вид работы

    @Enumerated(EnumType.STRING)
    private TicketType status;                  //Статус

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;                //ссылка на оборудование

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;                          //ссылка на создавшего заявку пользователя

    @ManyToOne
    @JoinColumn(name = "editor_user_id", nullable = true)
    @JsonBackReference
    private User editorUser;                    //ссылка на редактирующего заявку пользователя

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Supplies> supplies;           // Список материалов

    @Column(nullable = true)
    private String guestPhoneNumber;    // Номер телефона гостя

    @Column(nullable = true)
    private String guestDepartment;     // Отдел гостя

    @Column(nullable = true)
    private String userPhoneNumber;    // Номер телефона пользователя

    @Column(nullable = true)
    private String userDepartment;     // Отдел пользователя
}
