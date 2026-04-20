package pe.com.carlosh.tallyapi.budget;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.carlosh.tallyapi.budget.dto.BudgetRequestDTO;
import pe.com.carlosh.tallyapi.budget.dto.BudgetResponseDTO;
import pe.com.carlosh.tallyapi.core.dto.SetActiveRequestDTO;
import pe.com.carlosh.tallyapi.user.User;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<Page<BudgetResponseDTO>> findAll(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(budgetService.findAll(user.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(budgetService.findById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> create(
            @Valid @RequestBody BudgetRequestDTO req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.create(req, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDTO req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(budgetService.update(id, user.getId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        budgetService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActive(
            @PathVariable Long id,
            @Valid @RequestBody SetActiveRequestDTO req,
            @AuthenticationPrincipal User user) {
        budgetService.setActive(id, user.getId(), req.active());
        return ResponseEntity.noContent().build();
    }
}