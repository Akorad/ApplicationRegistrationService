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

    private static final Logger logger = Logger.getLogger(LdapService.class.getName());

    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    // Метод для тестирования подключения к LDAP-серверу
    private boolean testLdapConnection() {
        try {
            // Выполняем простой запрос, чтобы проверить подключение к LDAP
            ldapTemplate.search(ldapBase, "(objectClass=person)", (AttributesMapper<String>) attributes -> "Test");
            System.out.println("Подключение к LDAP успешно!");
            return true;  // Если запрос прошел без ошибок, подключение установлено
        } catch (Exception e) {
            System.out.println("Ошибка подключения к LDAP: " + e.getMessage());
            return false;  // Если ошибка, возвращаем false
        }
    }

    public LdapUserDetails getUserDetails(String username) {
        // Логирование начала запроса
        System.out.println("Запрос LDAP для пользователя: " + username);

        // Сначала тестируем подключение к LDAP
        if (!testLdapConnection()) {
            throw new RuntimeException("Не удалось подключиться к LDAP серверу.");
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
