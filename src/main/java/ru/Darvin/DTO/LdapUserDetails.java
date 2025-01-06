package ru.Darvin.DTO;

import lombok.Data;

@Data
public class LdapUserDetails {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String phoneNumber;
}
