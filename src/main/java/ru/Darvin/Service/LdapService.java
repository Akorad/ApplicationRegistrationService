package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    @Value("${spring.ldap.base}")
    private String ldapBase;

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
        String filter = "(uid=" + username + ")";

        try {
            // Указываем полный DN для поиска в подкаталоге ou=accounts
            String searchBase = "ou=accounts," + ldapBase;

            // Логируем перед выполнением запроса
            System.out.println("Поиск пользователя с фильтром: " + filter);
            System.out.println("Search Base: " + searchBase);

            // Запрос в LDAP
            List<LdapUserDetails> result = ldapTemplate.search(
                    searchBase,
                    filter,
                    (AttributesMapper<LdapUserDetails>) attributes -> mapToUserDetails(attributes, username)
            );

            // Проверка на наличие результатов
            if (result.isEmpty()) {
                System.out.println("Пользователь " + username + " не найден в LDAP.");
                return null;
            }

            // Логируем количество найденных результатов
            System.out.println("Найдено пользователей: " + result.size());

            // Печать атрибутов первого результата для отладки
            LdapUserDetails userDetails = result.get(0);
            System.out.println("Данные пользователя: " + userDetails);

            // Возвращаем первый результат
            return userDetails;

        } catch (Exception e) {
            // Логируем ошибку
            System.out.println("Ошибка при запросе LDAP для пользователя: " + username + ", " + e.getMessage());
            throw new RuntimeException("Ошибка при запросе LDAP для пользователя: "+ username +" Ошибка: "+ e.getMessage());
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
