package ru.Darvin.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;
import ru.Darvin.DTO.Mapper.UserMapperImpl;
import ru.Darvin.DTO.UserInfoDTO;
import ru.Darvin.DTO.UserTicketInfo;
import ru.Darvin.DTO.UserUpdateDto;
import ru.Darvin.Entity.Role;
import ru.Darvin.Entity.User;
import ru.Darvin.Repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.Darvin.Entity.Role.USER;

@Service
@RequiredArgsConstructor
public class UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private final UserRepository userRepository;

    /**
     * Сохранение пользователя.
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Создание пользователя.
     */
    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        return save(user);
    }

    /**
     * Редактирование пользователя.
     */
    public String updateUser(String userName, UserUpdateDto userUpdateDto) {
        User currentUser = getCurrentUser();

        if (!currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("У вас нет прав на редактирование пользователя.");
        }

        User user = getByUsername(userName);

        user.setRole(userUpdateDto.getRole());

        userRepository.save(user);
        return "Пользователь " + userName + " успешно обновлен.";
    }

    /**
     * Получить данные о пользователе по его имени пользователя.
     */
    public UserInfoDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return UserMapperImpl.INSTANCE.maptoInfoDTO(user);
    }

    /**
     * Получить список всех пользователей.
     */
    public List<UserInfoDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapperImpl.INSTANCE::maptoInfoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение пользователя по логину.
     */
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    /**
     * Получение списка администраторов.
     */
    public List<User> findAdmins() {
        return userRepository.findByRole(Role.ADMIN);
    }

    /**
     * Получение текущего пользователя.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Реализация метода загрузки OAuth2-пользователя.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(new OidcUserRequest(userRequest.getClientRegistration(), userRequest.getAccessToken(), null));

        String username = oidcUser.getAttribute("sub");

        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = User.builder()
                    .username(username)
                    .email(oidcUser.getAttribute("email"))
                    .firstName(oidcUser.getAttribute("given_name"))
                    .lastName(oidcUser.getAttribute("family_name"))
                    .role(USER)
                    .build();
            return userRepository.save(newUser);
        });

        return new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    /**
     * Сервис для загрузки пользователя по имени.
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public UserDetails findOrCreateUser(String username, Map<String, Object> userInfo) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(new BCryptPasswordEncoder().encode("default_password"));
            user.setRole(USER);
            user.setEmail((String) userInfo.get("email"));
            user.setFirstName((String) userInfo.get("given_name"));
            user.setLastName((String) userInfo.get("family_name"));
            userRepository.save(user);
        }

        return user;
    }

    public void createOrUpdateUser(LdapUserDetails ldapUserDetails) {
        Optional<User> optionalUser = userRepository.findByUsername(ldapUserDetails.getUsername());

        User user;
        if (optionalUser.isPresent()) {
            // Если пользователь существует, обновляем его данные
            user = optionalUser.get();
            user.setFirstName(ldapUserDetails.getFirstName());
            user.setLastName(ldapUserDetails.getLastName());
            user.setEmail(ldapUserDetails.getEmail());
            user.setDepartment(ldapUserDetails.getDepartment());
            user.setPhoneNumber(ldapUserDetails.getPhoneNumber());
        } else {
            // Если пользователь не найден, создаём нового
            user = new User();
            user.setUsername(ldapUserDetails.getUsername());
            user.setFirstName(ldapUserDetails.getFirstName());
            user.setLastName(ldapUserDetails.getLastName());
            user.setEmail(ldapUserDetails.getEmail());
            user.setDepartment(ldapUserDetails.getDepartment());
            user.setPhoneNumber(ldapUserDetails.getPhoneNumber());
            user.setRole(USER); // Роль по умолчанию
            // Устанавливаем временный пароль
            String defaultPassword = "default_password"; // Или пустой хэшированный пароль
            user.setPassword(defaultPassword);
        }

        // Сохраняем пользователя в базе данных
        userRepository.save(user);
    }

    public UserTicketInfo getTicketInfo() {
        User currentUser = getCurrentUser();
        UserTicketInfo userTicketInfo = new UserTicketInfo();
        userTicketInfo.setDepartment(currentUser.getDepartment());
        userTicketInfo.setPhoneNumber(currentUser.getPhoneNumber());
        return userTicketInfo;
    }
}
