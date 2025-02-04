package ru.Darvin.DTO.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.DTO.SupplyDTO;
import ru.Darvin.Entity.StockSupplies;
import ru.Darvin.Entity.Supplies;

@Mapper
public interface SuppliesMapperImpl {
    SuppliesMapperImpl INSTANCE = Mappers.getMapper(SuppliesMapperImpl.class);

    Supplies mapToSupplies(SuppliesDTO suppliesDTO);

    SuppliesDTO mapToSuppliesDTO (StockSupplies stockSupplies);
}
