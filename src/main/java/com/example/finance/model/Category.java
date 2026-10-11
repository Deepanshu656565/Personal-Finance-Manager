package com.example.finance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String name;
    private String type;
    private boolean customCategory;

    public Category() { }
    public Category(Long userId, String name, String type, boolean customCategory) {
        this.userId = userId; this.name = name; this.type = type; this.customCategory = customCategory;
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isCustomCategory() { return customCategory; }
}
