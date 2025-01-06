package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.logging.Logger;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    private static final Logger logger = Logger.getLogger(LdapService.class.getName());

    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public LdapUserDetails getUserDetails(String username) {
        // Логирование начала запроса
        System.out.println("Запрос LDAP для пользователя: " + username);

        // Тест подключения к LDAP серверу
        if (!testLdapConnection()) {
            System.out.println("Не удалось подключиться к LDAP серверу.");
            return null; // Возвращаем null, если не удалось подключиться
        }

        // Фильтр для поиска пользователя по имени
        EqualsFilter filter = new EqualsFilter("uid", username);

        try {
            // Логируем перед выполнением запроса
            System.out.println("Поиск пользователя с фильтром: " + filter.encode());

            // Запрос в LDAP
            var result = ldapTemplate.search(
                    ldapBase,
                    filter.encode(),
                    (AttributesMapper<LdapUserDetails>) attributes -> mapToUserDetails(attributes, username)
            );

            if (result.isEmpty()) {
                System.out.println("Пользователь " + username + " не найден в LDAP.");
                return null;
            }

            LdapUserDetails userDetails = result.stream().findFirst().orElse(null);
            if (userDetails != null) {
                System.out.println("Данные пользователя " + username + " успешно получены.");
            }
            return userDetails;

        } catch (Exception e) {
            // Логируем ошибку
            System.out.println("Ошибка при запросе LDAP для пользователя: " + username + ", " + e.getMessage());
            throw new RuntimeException("Ошибка при запросе LDAP для пользователя: " + username, e);
        }
    }

    private boolean testLdapConnection() {
        try {
            // Пробуем выполнить запрос к LDAP серверу для проверки соединения
            ldapTemplate.search("", "(objectClass=*)", (AttributesMapper<Object>) attributes -> null);
            System.out.println("LDAP сервер доступен.");
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка подключения к LDAP серверу: " + e.getMessage());
            return false;
        }
    }

    private LdapUserDetails mapToUserDetails(Attributes attributes, String username) throws NamingException {
        LdapUserDetails details = new LdapUserDetails();
        details.setUsername(username);
        details.setFirstName(getAttribute(attributes, "firstname"));
        details.setLastName(getAttribute(attributes, "lastname"));
        details.setEmail(getAttribute(attributes, "mail"));
        details.setDepartment(getAttribute(attributes, "department"));
        details.setPhoneNumber(getAttribute(attributes, "displayphone"));
        return details;
    }

    private String getAttribute(Attributes attributes, String attributeName) throws NamingException {
        if (attributes.get(attributeName) != null) {
            return attributes.get(attributeName).get().toString();
        } else {
            System.out.println("Атрибут " + attributeName + " отсутствует.");
            return null;
        }
    }

}
