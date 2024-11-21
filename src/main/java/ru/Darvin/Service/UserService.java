package ru.Darvin.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.Mapper.UserMapperImpl;
import ru.Darvin.DTO.UserInfoDTO;
import ru.Darvin.DTO.UserUpdateDto;
import ru.Darvin.Entity.Role;
import ru.Darvin.Entity.User;
import ru.Darvin.Repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    //Сохранение пользователя
    public User save(User user){
        return userRepository.save(user);
    }

    //Создание пользователя
    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        return save(user);
    }

    //Редактирование пользователя
    public String updateUser(String userName, UserUpdateDto userUpdateDto){
        User currentUser = getCurrentUser();

        if(!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getUsername().equals(userName)){
            throw new AccessDeniedException("У вас нет прав на редактирование этого пользователя.");
        }

        User user = getByUsername(userName);

        User updatedUser = UserMapperImpl.INSTANCE.maptoUpdateUser(userUpdateDto);

        updatedUser.setId(user.getId());
        updatedUser.setUsername(user.getUsername());
        updatedUser.setPassword(user.getPassword());
        updatedUser.setRole(user.getRole());
        if (currentUser.getRole().equals(Role.ADMIN)) {
            updatedUser.setRole(userUpdateDto.getRole());
        }
        userRepository.save(updatedUser);

        return "Пользователь " + userName + " успешно обновлен.";
    }

    // Получить данные о пользователе по его имени пользователя
    public UserInfoDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return UserMapperImpl.INSTANCE.maptoInfoDTO(user);
    }

    // Получить список всех пользователей
    public List<UserInfoDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapperImpl.INSTANCE::maptoInfoDTO)
                .collect(Collectors.toList());
    }

    //Получение пользователя по логину
    public User getByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("Пользователь не найден"));
    }

    //Получение списка администраторов
    public List<User> findAdmins(){
        return userRepository.findByRole(Role.ADMIN);
    }


    // Получение текущего пользователя
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public UserDetailsService userDetailsService(){
        return this::getByUsername;
    }


}
