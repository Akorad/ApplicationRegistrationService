package ru.Darvin.DTO;

import lombok.Data;
import ru.Darvin.Entity.Equipment;
import ru.Darvin.Entity.Supplies;
import ru.Darvin.Entity.TicketType;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketInfoDTO {
    private Long ticketNumber;                  //Порядковый номер заявки
    private LocalDateTime createdDate;          //Дата создания
    private LocalDateTime endDate;              //Дата закрытия
    private LocalDateTime readyDate;              //Дата закрытия
    private Boolean refilling;
    private String descriptionOfTheProblem;     //Заявленная неисправность
    private String detectedProblem;             //Обнаруженная неисправность
    private String comments;                    //Комментарии
    private String typeOfWork;                  //Вид работы
    private TicketType status;                  //Статус
    private UserDTO user;                       //ссылка на создавшего заявку пользователя
    private UserDTO editorUser;                 //ссылка на редактирующего заявку пользователя
    private String guestPhoneNumber;            //Номер телефона гостя
    private String guestDepartment;             //Отдел гостя
    private String userPhoneNumber;             //Номер телефона пользователя
    private String userDepartment;              //Отдел пользователя


    private Equipment equipment;                //ссылка на оборудование


    private List<Supplies> supplies;            //Список материалов

}
