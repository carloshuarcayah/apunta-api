package pe.com.carlosh.tallyapi.expense.dto;

import java.math.BigDecimal;

public record ExpenseStatsDTO(
        BigDecimal total,
        BigDecimal thisMonth,
        BigDecimal lastMonth,
        BigDecimal vsLastMonthPercent,
        long count,
        long thisMonthCount
) {
}
