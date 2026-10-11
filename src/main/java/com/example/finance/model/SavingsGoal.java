package com.example.finance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "savings_goals")
public class SavingsGoal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;

    public SavingsGoal() { }
    public SavingsGoal(Long userId, String goalName, BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate) {
        this.userId = userId; this.goalName = goalName; this.targetAmount = targetAmount; this.targetDate = targetDate; this.startDate = startDate;
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getGoalName() { return goalName; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public LocalDate getTargetDate() { return targetDate; }
    public LocalDate getStartDate() { return startDate; }
    public void setTargetAmount(BigDecimal amount) { this.targetAmount = amount; }
    public void setTargetDate(LocalDate date) { this.targetDate = date; }
}
