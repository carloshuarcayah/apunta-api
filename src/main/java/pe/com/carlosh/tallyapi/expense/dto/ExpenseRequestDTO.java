package pe.com.carlosh.tallyapi.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequestDTO(
        @NotNull @Positive BigDecimal amount,
        String description,
        Long categoryId,
        Long budgetId
) {}