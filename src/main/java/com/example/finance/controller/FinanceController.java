package com.example.finance.controller;

import com.example.finance.model.*;
import com.example.finance.service.FinanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FinanceController {
    private final FinanceService service;
    public FinanceController(FinanceService service) { this.service = service; }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        AppUser user = service.register(body);
        Map<String, Object> result = new LinkedHashMap<>(); result.put("message", "User registered successfully"); result.put("userId", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body, HttpSession session) {
        AppUser user = service.authenticate(body); session.setAttribute("userId", user.getId());
        return Map.of("message", "Login successful");
    }
    @PostMapping("/auth/logout")
    public Map<String, Object> logout(HttpSession session) {
        if (session.getAttribute("userId") == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
        session.invalidate(); return Map.of("message", "Logout successful");
    }
    private Long userId(HttpSession session) {
        Object id = session.getAttribute("userId");
        if (id == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
        return (Long) id;
    }

    @GetMapping("/categories")
    public Map<String, Object> categories(HttpSession session) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Category category : service.getCategories(userId(session))) {
            Map<String, Object> row = new LinkedHashMap<>(); row.put("id", category.getId()); row.put("name", category.getName()); row.put("type", category.getType()); row.put("isCustom", category.isCustomCategory()); result.add(row);
        }
        return Map.of("categories", result);
    }
    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> addCategory(@RequestBody Map<String, Object> body, HttpSession session) {
        Category category = service.addCategory(userId(session), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", category.getName(), "type", category.getType(), "isCustom", true));
    }
    @DeleteMapping("/categories/{name}")
    public Map<String, Object> deleteCategory(@PathVariable String name, HttpSession session) { service.deleteCategory(userId(session), name); return Map.of("message", "Category deleted successfully"); }

    @PostMapping("/transactions")
    public ResponseEntity<FinanceTransaction> addTransaction(@RequestBody Map<String, Object> body, HttpSession session) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addTransaction(userId(session), body));
    }
    @GetMapping("/transactions")
    public Map<String, Object> transactions(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String category, @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type, HttpSession session) {
        List<FinanceTransaction> list = service.getTransactions(userId(session), startDate, endDate, category, type);
        if (categoryId != null) list.removeIf(item -> !categoryId.equals(item.getCategoryId()));
        return Map.of("transactions", list);
    }
    @PutMapping("/transactions/{id}")
    public FinanceTransaction updateTransaction(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) { return service.updateTransaction(userId(session), id, body); }
    @DeleteMapping("/transactions/{id}")
    public Map<String, Object> deleteTransaction(@PathVariable Long id, HttpSession session) { service.deleteTransaction(userId(session), id); return Map.of("message", "Transaction deleted successfully"); }

    @PostMapping("/goals")
    public ResponseEntity<Map<String, Object>> addGoal(@RequestBody Map<String, Object> body, HttpSession session) { return ResponseEntity.status(HttpStatus.CREATED).body(service.getGoalAfterCreate(userId(session), body)); }
    @GetMapping("/goals")
    public Map<String, Object> goals(HttpSession session) { return Map.of("goals", service.getGoals(userId(session))); }
    @GetMapping("/goals/{id}")
    public Map<String, Object> goal(@PathVariable Long id, HttpSession session) { return service.getGoal(userId(session), id); }
    @PutMapping("/goals/{id}")
    public Map<String, Object> updateGoal(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) { return service.updateGoal(userId(session), id, body); }
    @DeleteMapping("/goals/{id}")
    public Map<String, Object> deleteGoal(@PathVariable Long id, HttpSession session) { service.deleteGoal(userId(session), id); return Map.of("message", "Goal deleted successfully"); }

    @GetMapping("/reports/monthly/{year}/{month}")
    public Map<String, Object> monthlyReport(@PathVariable int year, @PathVariable int month, HttpSession session) { return service.monthlyReport(userId(session), year, month); }
    @GetMapping("/reports/yearly/{year}")
    public Map<String, Object> yearlyReport(@PathVariable int year, HttpSession session) { return service.yearlyReport(userId(session), year); }
}
