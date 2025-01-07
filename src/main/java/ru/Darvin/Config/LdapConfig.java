//package ru.Darvin.Config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.ldap.core.LdapTemplate;
//import org.springframework.ldap.core.support.LdapContextSource;
//
//@Configuration
//public class LdapConfig {
//
//    @Bean
//    public LdapContextSource ldapContextSource() {
//        LdapContextSource contextSource = new LdapContextSource();
//        contextSource.setUrl("ldap://lk.ustu:389");
//        contextSource.setBase("dc=ams,dc=ulstu,dc=ru");
//        contextSource.setUserDn("cn=repair,ou=services,dc=ams,dc=ulstu,dc=ru"); // Здесь указывается DN пользователя
//        contextSource.setPassword("J*t9L_6heQ86M+a5%"); // Здесь указывается пароль
//        contextSource.setPooled(false);
//        return contextSource;
//    }
//
//    @Bean
//    public LdapTemplate ldapTemplate() {
//        return new LdapTemplate(ldapContextSource());
//    }
//}
