package pe.com.carlosh.tallyapi.expense.dto;

import java.math.BigDecimal;

public record CategoryTotalDTO(Long categoryId, BigDecimal total, Long count) {}