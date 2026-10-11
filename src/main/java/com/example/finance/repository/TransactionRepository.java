package com.example.finance.repository;
import com.example.finance.model.FinanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface TransactionRepository extends JpaRepository<FinanceTransaction, Long> {
    List<FinanceTransaction> findByUserIdOrderByDateDescIdDesc(Long userId);
    Optional<FinanceTransaction> findByIdAndUserId(Long id, Long userId);
    List<FinanceTransaction> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate start, LocalDate end);
    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);
}
