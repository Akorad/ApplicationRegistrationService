package ru.Darvin.DTO.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.Darvin.DTO.EquipmentDTO;
import ru.Darvin.Entity.Equipment;

@Mapper
public interface EquipmentMapperImpl {
    EquipmentMapperImpl INSTANCE = Mappers.getMapper(EquipmentMapperImpl.class);

    Equipment mapToEntity(EquipmentDTO equipmentDTO);
}
