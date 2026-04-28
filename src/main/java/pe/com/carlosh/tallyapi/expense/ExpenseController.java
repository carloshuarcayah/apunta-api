package pe.com.carlosh.tallyapi.expense;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.carlosh.tallyapi.expense.dto.ExpenseRequestDTO;
import pe.com.carlosh.tallyapi.expense.dto.ExpenseResponseDTO;
import pe.com.carlosh.tallyapi.expense.dto.ExpenseListResponseDTO;
import pe.com.carlosh.tallyapi.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;


    //FIND ALL AND FIND ALL BY CATEGORY

    @GetMapping
    public ResponseEntity<ExpenseListResponseDTO> findAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long budgetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(expenseService.findByFilters(user.getId(), categoryId, budgetId, from, to, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.findById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> create(
            @Valid @RequestBody ExpenseRequestDTO req,
            @AuthenticationPrincipal User user) {

        ExpenseResponseDTO response = expenseService.create(req, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestDTO req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.update(id, req, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        expenseService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotal(
            @RequestParam(required = false) Long categoryId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.getTotal(user.getId(), categoryId));
    }

    @GetMapping("/calendar")
    public ResponseEntity<Map<String, BigDecimal>> getCalendar(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.getCalendar(user.getId(), year, month));
    }
}