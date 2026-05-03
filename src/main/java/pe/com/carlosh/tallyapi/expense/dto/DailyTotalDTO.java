package pe.com.carlosh.tallyapi.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTotalDTO(LocalDate date, BigDecimal total) {}