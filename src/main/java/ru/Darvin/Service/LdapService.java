package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

@Service
public class LdapService {

    @Value("${spring.ldap.base}")
    private String ldapBase;
    @Value("${spring.ldap.urls}")
    private String ldapUrl;
    @Value("${spring.ldap.username}")
    private String ldapUserDn;
    @Value("${spring.ldap.password}")
    private String ldapPassword;

    public LdapUserDetails getUserDetails(String username) {
        // Логирование начала запроса
        System.out.println("Запрос LDAP для пользователя: " + username);

        // Тест подключения к LDAP серверу
        LdapContext context = createLdapContext();
        if (context == null) {
            System.out.println("Не удалось подключиться к LDAP серверу.");
            return null;
        }

        // Формируем базу поиска и фильтр
        String searchBase = "ou=accounts," + ldapBase;
        String filter = "(uid=" + username + ")";
        System.out.println("Поиск пользователя с фильтром: " + filter);
        System.out.println("Search Base: " + searchBase);

        try {
            // Параметры поиска
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); // Поиск во всех подкаталогах

            // Выполняем поиск
            NamingEnumeration<SearchResult> results = context.search(searchBase, filter, searchControls);

            if (!results.hasMore()) {
                System.out.println("Пользователь " + username + " не найден в LDAP.");
                return null;
            }

            // Получаем первую запись результата
            SearchResult searchResult = results.next();
            Attributes attributes = searchResult.getAttributes();

            // Маппим атрибуты в объект LdapUserDetails
            LdapUserDetails userDetails = mapToUserDetails(attributes, username);
            System.out.println("Данные пользователя: " + userDetails);

            // Возвращаем найденного пользователя
            return userDetails;

        } catch (Exception e) {
            System.out.println("Ошибка при запросе LDAP для пользователя: " + username + ", " + e.getMessage());
            throw new RuntimeException("Ошибка при запросе LDAP для пользователя: " + username + " Ошибка: " + e.getMessage());
        } finally {
            try {
                context.close();
            } catch (NamingException e) {
                System.out.println("Ошибка при закрытии LDAP контекста: " + e.getMessage());
            }
        }
    }

    private LdapContext createLdapContext() {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapUserDn);
        env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

        try {
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            System.out.println("Ошибка при создании LDAP контекста: " + e.getMessage());
            return null;
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
