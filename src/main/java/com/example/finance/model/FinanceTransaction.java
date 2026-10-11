package com.example.finance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "finance_transactions")
public class FinanceTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private LocalDate date;
    private Long categoryId;
    private String category;
    private String type;
    private String description;

    public FinanceTransaction() { }
    public FinanceTransaction(Long userId, BigDecimal amount, LocalDate date, Long categoryId, String category, String type, String description) {
        this.userId = userId; this.amount = amount; this.date = date; this.categoryId = categoryId; this.category = category; this.type = type; this.description = description;
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public Long getCategoryId() { return categoryId; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCategory(String category) { this.category = category; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
}
