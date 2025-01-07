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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
        System.out.println("Запрос LDAP для пользователя: " + username);

        LdapContext context = createLdapContext();
        if (context == null) {
            System.out.println("Не удалось подключиться к LDAP серверу.");
            return null;
        }

        String searchBase = "ou=accounts," + ldapBase;
        String filter = "(&(uid=" + username + ")(|(objectClass=ulstuPerson)(objectClass=ulstuCourse)(objectClass=ulstuJob)))";
        System.out.println("Поиск пользователя с фильтром: " + filter);
        System.out.println("Search Base: " + searchBase);

        try {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> results = context.search(searchBase, filter, searchControls);

            if (!results.hasMore()) {
                System.out.println("Пользователь " + username + " не найден в LDAP.");
                return null;
            }

            List<String> departments = new ArrayList<>();
            String firstName = null, lastName = null, email = null, phoneNumber = null;

            while (results.hasMore()) {
                SearchResult searchResult = results.next();
                Attributes attributes = searchResult.getAttributes();

                if (attributes.get("firstName") != null) {
                    firstName = getAttribute(attributes, "firstName");
                }
                if (attributes.get("lastName") != null) {
                    lastName = getAttribute(attributes, "lastName");
                }
                if (attributes.get("displayMail") != null) {
                    email = getAttribute(attributes, "displayMail");
                }
                if (attributes.get("displayPhone") != null) {
                    phoneNumber = getAttribute(attributes, "displayPhone");
                }
                if (attributes.get("department") != null) {
                    NamingEnumeration<?> departmentsEnum = attributes.get("department").getAll();
                    while (departmentsEnum.hasMore()) {
                        departments.add(departmentsEnum.next().toString());
                    }
                }
            }

            String department = String.join(", ", departments);

            LdapUserDetails userDetails = new LdapUserDetails();
            userDetails.setUsername(username);
            userDetails.setFirstName(firstName);
            userDetails.setLastName(lastName);
            userDetails.setEmail(email);
            userDetails.setPhoneNumber(phoneNumber);
            userDetails.setDepartment(department);

            System.out.println("Данные пользователя: " + userDetails);
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

    private String getAttribute(Attributes attributes, String attributeName) throws NamingException {
        if (attributes.get(attributeName) != null) {
            return attributes.get(attributeName).get().toString();
        } else {
            System.out.println("Атрибут " + attributeName + " отсутствует.");
            return null;
        }
    }
}
