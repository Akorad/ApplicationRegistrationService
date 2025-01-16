package ru.Darvin.DTO.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import ru.Darvin.DTO.IssueByMOLHistory;
import ru.Darvin.DTO.IssueByMOLUpdate;
import ru.Darvin.Entity.SuppliesIssue;

@Mapper
public interface IssueMapperImpl {
    IssueMapperImpl INSTANCE = Mappers.getMapper(IssueMapperImpl.class);

    IssueByMOLHistory mapToMOLHistory (SuppliesIssue suppliesIssue);

    void updateFromDto(IssueByMOLUpdate dto, @MappingTarget SuppliesIssue entity);

}
