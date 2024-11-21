package ru.Darvin.DTO.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.Darvin.DTO.UserDTO;
import ru.Darvin.DTO.UserInfoDTO;
import ru.Darvin.DTO.UserUpdateDto;
import ru.Darvin.Entity.User;

@Mapper
public interface UserMapperImpl {
    UserMapperImpl INSTANCE = Mappers.getMapper(UserMapperImpl.class);

    UserDTO maptoUserDTO (User user);

    @Mapping(target = "role", ignore = true)
    User maptoUpdateUser (UserUpdateDto userUpdateDto);

    UserInfoDTO maptoInfoDTO (User user);

    UserUpdateDto mapToUserUpdateDto (User user);
}
