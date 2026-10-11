package com.example.finance.service;

import com.example.finance.model.*;
import com.example.finance.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class FinanceService {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final TransactionRepository transactions;
    private final GoalRepository goals;
    private final PasswordEncoder passwordEncoder;

    public FinanceService(UserRepository users, CategoryRepository categories, TransactionRepository transactions, GoalRepository goals, PasswordEncoder passwordEncoder) {
        this.users = users; this.categories = categories; this.transactions = transactions; this.goals = goals; this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(Map<String, Object> body) {
        String username = requiredText(body, "username");
        String password = requiredText(body, "password");
        String fullName = requiredText(body, "fullName");
        String phone = requiredText(body, "phoneNumber");
        if (!username.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw bad("Username must be a valid email address");
        if (password.length() < 8) throw bad("Password must contain at least 8 characters");
        if (users.existsByUsername(username)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        AppUser user = users.save(new AppUser(username, passwordEncoder.encode(password), fullName, phone));
        addDefaultCategories(user.getId());
        return user;
    }

    public AppUser authenticate(Map<String, Object> body) {
        String username = requiredText(body, "username");
        String password = requiredText(body, "password");
        AppUser user = users.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        return user;
    }

    private void addDefaultCategories(Long userId) {
        categories.save(new Category(userId, "Salary", "INCOME", false));
        String[] names = {"Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities"};
        for (String name : names) categories.save(new Category(userId, name, "EXPENSE", false));
    }

    public List<Category> getCategories(Long userId) { return categories.findByUserId(userId); }
    public Category addCategory(Long userId, Map<String, Object> body) {
        String name = requiredText(body, "name");
        String type = requiredText(body, "type").toUpperCase();
        if (!type.equals("INCOME") && !type.equals("EXPENSE")) throw bad("Type must be INCOME or EXPENSE");
        if (categories.findByUserIdAndNameIgnoreCase(userId, name).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        return categories.save(new Category(userId, name, type, true));
    }
    public void deleteCategory(Long userId, String name) {
        Category category = categories.findByUserIdAndNameIgnoreCase(userId, name).orElseThrow(() -> notFound("Category not found"));
        if (!category.isCustomCategory()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Default categories cannot be deleted");
        if (transactions.existsByUserIdAndCategoryId(userId, category.getId())) throw bad("Category is used by a transaction");
        categories.delete(category);
    }

    public FinanceTransaction addTransaction(Long userId, Map<String, Object> body) {
        BigDecimal amount = amount(body.get("amount"));
        LocalDate date = parseDate(body.get("date"), "date");
        if (date.isAfter(LocalDate.now())) throw bad("Date cannot be in the future");
        String categoryName = requiredText(body, "category");
        Category category = categories.findByUserIdAndNameIgnoreCase(userId, categoryName).orElseThrow(() -> bad("Category not found"));
        String description = optionalText(body.get("description"));
        return transactions.save(new FinanceTransaction(userId, amount, date, category.getId(), category.getName(), category.getType(), description));
    }
    public List<FinanceTransaction> getTransactions(Long userId, String startDate, String endDate, String categoryName, String type) {
        List<FinanceTransaction> list;
        if (startDate != null || endDate != null) {
            LocalDate start = startDate == null ? LocalDate.of(1970, 1, 1) : parseDate(startDate, "startDate");
            LocalDate end = endDate == null ? LocalDate.now() : parseDate(endDate, "endDate");
            if (start.isAfter(end)) throw bad("startDate must be before endDate");
            list = transactions.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);
        } else list = transactions.findByUserIdOrderByDateDescIdDesc(userId);
        List<FinanceTransaction> result = new ArrayList<>();
        for (FinanceTransaction item : list) {
            if (categoryName != null && !item.getCategory().equalsIgnoreCase(categoryName)) continue;
            if (type != null && !item.getType().equalsIgnoreCase(type)) continue;
            result.add(item);
        }
        return result;
    }
    public FinanceTransaction updateTransaction(Long userId, Long id, Map<String, Object> body) {
        FinanceTransaction item = transactions.findByIdAndUserId(id, userId).orElseThrow(() -> notFound("Transaction not found"));
        if (body.containsKey("date")) throw bad("Transaction date cannot be changed");
        if (body.containsKey("amount")) item.setAmount(amount(body.get("amount")));
        if (body.containsKey("description")) item.setDescription(optionalText(body.get("description")));
        if (body.containsKey("category")) {
            Category category = categories.findByUserIdAndNameIgnoreCase(userId, requiredText(body, "category")).orElseThrow(() -> bad("Category not found"));
            item.setCategoryId(category.getId()); item.setCategory(category.getName()); item.setType(category.getType());
        }
        return transactions.save(item);
    }
    public void deleteTransaction(Long userId, Long id) {
        FinanceTransaction item = transactions.findByIdAndUserId(id, userId).orElseThrow(() -> notFound("Transaction not found"));
        transactions.delete(item);
    }

    public SavingsGoal addGoal(Long userId, Map<String, Object> body) {
        String name = requiredText(body, "goalName");
        BigDecimal target = amount(body.get("targetAmount"));
        LocalDate targetDate = parseDate(body.get("targetDate"), "targetDate");
        if (!targetDate.isAfter(LocalDate.now())) throw bad("Target date must be in the future");
        LocalDate startDate = body.get("startDate") == null ? LocalDate.now() : parseDate(body.get("startDate"), "startDate");
        return goals.save(new SavingsGoal(userId, name, target, targetDate, startDate));
    }
    public List<Map<String, Object>> getGoals(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SavingsGoal goal : goals.findByUserId(userId)) result.add(goalDetails(userId, goal));
        return result;
    }
    public Map<String, Object> getGoalAfterCreate(Long userId, Map<String, Object> body) {
        SavingsGoal goal = addGoal(userId, body);
        return goalDetails(userId, goal);
    }
    public Map<String, Object> getGoal(Long userId, Long id) {
        SavingsGoal goal = goals.findByIdAndUserId(id, userId).orElseThrow(() -> notFound("Goal not found"));
        return goalDetails(userId, goal);
    }
    public Map<String, Object> updateGoal(Long userId, Long id, Map<String, Object> body) {
        SavingsGoal goal = goals.findByIdAndUserId(id, userId).orElseThrow(() -> notFound("Goal not found"));
        if (body.containsKey("targetAmount")) goal.setTargetAmount(amount(body.get("targetAmount")));
        if (body.containsKey("targetDate")) {
            LocalDate date = parseDate(body.get("targetDate"), "targetDate");
            if (!date.isAfter(LocalDate.now())) throw bad("Target date must be in the future");
            goal.setTargetDate(date);
        }
        goals.save(goal);
        return goalDetails(userId, goal);
    }
    public void deleteGoal(Long userId, Long id) {
        SavingsGoal goal = goals.findByIdAndUserId(id, userId).orElseThrow(() -> notFound("Goal not found"));
        goals.delete(goal);
    }
    private Map<String, Object> goalDetails(Long userId, SavingsGoal goal) {
        BigDecimal income = BigDecimal.ZERO, expense = BigDecimal.ZERO;
        for (FinanceTransaction item : transactions.findByUserIdOrderByDateDescIdDesc(userId)) {
            if (item.getDate().isBefore(goal.getStartDate())) continue;
            if (item.getType().equals("INCOME")) income = income.add(item.getAmount());
            else expense = expense.add(item.getAmount());
        }
        BigDecimal progress = income.subtract(expense);
        if (progress.signum() < 0) progress = BigDecimal.ZERO;
        BigDecimal percent = progress.multiply(new BigDecimal("100")).divide(goal.getTargetAmount(), 2, java.math.RoundingMode.HALF_UP);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", goal.getId()); map.put("goalName", goal.getGoalName()); map.put("targetAmount", goal.getTargetAmount());
        map.put("targetDate", goal.getTargetDate()); map.put("startDate", goal.getStartDate()); map.put("currentProgress", progress);
        map.put("progressPercentage", percent); map.put("remainingAmount", goal.getTargetAmount().subtract(progress).max(BigDecimal.ZERO));
        return map;
    }

    public Map<String, Object> monthlyReport(Long userId, int year, int month) {
        if (month < 1 || month > 12) throw bad("Month must be between 1 and 12");
        LocalDate start = LocalDate.of(year, month, 1), end = start.withDayOfMonth(start.lengthOfMonth());
        return report(userId, year, month, start, end);
    }
    public Map<String, Object> yearlyReport(Long userId, int year) {
        return report(userId, year, null, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }
    private Map<String, Object> report(Long userId, int year, Integer month, LocalDate start, LocalDate end) {
        Map<String, BigDecimal> income = new LinkedHashMap<>(), expense = new LinkedHashMap<>();
        for (FinanceTransaction item : transactions.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end)) {
            Map<String, BigDecimal> target = item.getType().equals("INCOME") ? income : expense;
            target.put(item.getCategory(), target.getOrDefault(item.getCategory(), BigDecimal.ZERO).add(item.getAmount()));
        }
        BigDecimal totalIncome = sum(income), totalExpense = sum(expense);
        Map<String, Object> result = new LinkedHashMap<>();
        if (month != null) result.put("month", month);
        result.put("year", year); result.put("totalIncome", income); result.put("totalExpenses", expense); result.put("netSavings", totalIncome.subtract(totalExpense));
        return result;
    }
    private BigDecimal sum(Map<String, BigDecimal> values) { BigDecimal total = BigDecimal.ZERO; for (BigDecimal value : values.values()) total = total.add(value); return total; }
    private String requiredText(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().trim().isEmpty()) throw bad(key + " is required");
        return value.toString().trim();
    }
    private String optionalText(Object value) { return value == null ? "" : value.toString().trim(); }
    private BigDecimal amount(Object value) {
        try { BigDecimal result = new BigDecimal(value.toString()); if (result.signum() <= 0) throw bad("Amount must be greater than zero"); return result; }
        catch (NullPointerException | NumberFormatException ex) { throw bad("Amount must be a positive number"); }
    }
    private LocalDate parseDate(Object value, String field) {
        try { return LocalDate.parse(value.toString()); }
        catch (Exception ex) { throw bad(field + " must use YYYY-MM-DD format"); }
    }
    private ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
}
