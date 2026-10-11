package com.example.finance.repository;
import com.example.finance.model.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface GoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUserId(Long userId);
    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);
}
