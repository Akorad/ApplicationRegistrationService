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
import java.util.List;

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
        System.out.println("Запрос LDAP для пользователя: " + username);

        if (!testLdapConnection()) {
            System.out.println("Не удалось подключиться к LDAP серверу.");
            return null;
        }

        String filter = "(uid=" + username + ")";

        try {
            String searchBase = "ou=accounts," + ldapBase;

            System.out.println("Поиск пользователя с фильтром: " + filter);
            System.out.println("Search Base: " + searchBase);


            // Запрос LDAP и получение сырых атрибутов
            var results = ldapTemplate.search(
                    searchBase,
                    filter,
                    (AttributesMapper<Attributes>) attributes -> attributes
            );

            if (results.isEmpty()) {
                System.out.println("Пользователь " + username + " не найден в LDAP.");
                return null;
            }

            System.out.println("Найдено пользователей: " + results.size());

            // Логируем атрибуты всех записей
            results.forEach(attributes -> {
                try {
                    System.out.println("Атрибуты записи:");
                    var attributeNames = attributes.getAll();
                    while (attributeNames.hasMore()) {
                        var attribute = attributeNames.next();
                        System.out.println(attribute);
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка при чтении атрибутов: " + e.getMessage());
                }
            });

            // Применяем маппинг к первому результату
            var firstResult = results.get(0);
            LdapUserDetails userDetails = mapToUserDetails(firstResult, username);

            if (userDetails != null) {
                System.out.println("Данные пользователя " + username + ": " + userDetails);
            }

            return userDetails;

        } catch (Exception e) {
            System.out.println("Ошибка при запросе LDAP для пользователя: " + username + ", " + e.getMessage());
            throw new RuntimeException("Ошибка при запросе LDAP для пользователя: " + username + " Ошибка: " + e.getMessage());
        }
    }


    private boolean testLdapConnection() {
        try {
            // Пробуем выполнить запрос к LDAP серверу для проверки соединения
            ldapTemplate.search("dc=ams,dc=ulstu,dc=ru", "(objectClass=*)", (AttributesMapper<Object>) attributes -> null);
            System.out.println("LDAP сервер доступен.");
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка подключения к LDAP серверу: " + e.getMessage());
            return false;
        }
    }

    private LdapUserDetails mapToUserDetails(Attributes attributes, String username) throws NamingException {
        try {
            System.out.println("Маппинг атрибутов для пользователя: " + username);

            LdapUserDetails details = new LdapUserDetails();
            details.setUsername(username);
            details.setFirstName(getAttribute(attributes, "firstName"));
            details.setLastName(getAttribute(attributes, "lastName"));
            details.setEmail(getAttribute(attributes, "displayMail"));
            details.setDepartment(getAttribute(attributes, "department"));
            details.setPhoneNumber(getAttribute(attributes, "displayPhone"));

            return details;
        } catch (Exception e) {
            System.out.println("Ошибка mapToUserDetails: " + e.getMessage());
            return null;
        }
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
