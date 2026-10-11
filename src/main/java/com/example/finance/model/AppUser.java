package com.example.finance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String phoneNumber;

    public AppUser() { }
    public AppUser(String username, String password, String fullName, String phoneNumber) {
        this.username = username; this.password = password; this.fullName = fullName; this.phoneNumber = phoneNumber;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
}
