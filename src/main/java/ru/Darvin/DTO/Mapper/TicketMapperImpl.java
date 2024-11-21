package ru.Darvin.DTO.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.Darvin.DTO.TicketInfoDTO;
import ru.Darvin.Entity.Ticket;

@Mapper
public interface TicketMapperImpl {
    TicketMapperImpl INSTANCE = Mappers.getMapper(TicketMapperImpl.class);

    TicketInfoDTO mapToInfoDTO(Ticket ticket);
}
