package pe.com.carlosh.tallyapi.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.carlosh.tallyapi.category.Category;
import pe.com.carlosh.tallyapi.expense.dto.CategoryTotalDTO;
import pe.com.carlosh.tallyapi.expense.dto.DailyTotalDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ===== Búsquedas básicas =====

    Page<Expense> findByUserIdAndActiveTrue(Long userId, Pageable pageable);

    // ===== Búsqueda con filtros dinámicos (incluye fetch para evitar N+1) =====

    @Query(value = """
            SELECT e FROM Expense e
            LEFT JOIN FETCH e.category
            LEFT JOIN FETCH e.budget
            WHERE e.user.id = :userId AND e.active = true
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:budgetId IS NULL OR e.budget.id = :budgetId)
              AND (:from IS NULL OR e.expenseDate >= :from)
              AND (:to IS NULL OR e.expenseDate <= :to)
            """,
            countQuery = """
            SELECT COUNT(e) FROM Expense e
            WHERE e.user.id = :userId AND e.active = true
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:budgetId IS NULL OR e.budget.id = :budgetId)
              AND (:from IS NULL OR e.expenseDate >= :from)
              AND (:to IS NULL OR e.expenseDate <= :to)
            """)
    Page<Expense> findByFilters(@Param("userId") Long userId,
                                @Param("categoryId") Long categoryId,
                                @Param("budgetId") Long budgetId,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to,
                                Pageable pageable);

    // ===== Suma con filtros dinámicos (reemplaza 4 métodos antiguos) =====

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM Expense e
            WHERE e.user.id = :userId AND e.active = true
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:budgetId IS NULL OR e.budget.id = :budgetId)
              AND (:from IS NULL OR e.expenseDate >= :from)
              AND (:to IS NULL OR e.expenseDate <= :to)
            """)
    BigDecimal sumByFilters(@Param("userId") Long userId,
                            @Param("categoryId") Long categoryId,
                            @Param("budgetId") Long budgetId,
                            @Param("from") LocalDate from,
                            @Param("to") LocalDate to);

    // ===== Conteo con filtros dinámicos (reemplaza 3 métodos antiguos) =====

    @Query("""
            SELECT COUNT(e) FROM Expense e
            WHERE e.user.id = :userId AND e.active = true
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:budgetId IS NULL OR e.budget.id = :budgetId)
              AND (:from IS NULL OR e.expenseDate >= :from)
              AND (:to IS NULL OR e.expenseDate <= :to)
            """)
    long countByFilters(@Param("userId") Long userId,
                        @Param("categoryId") Long categoryId,
                        @Param("budgetId") Long budgetId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

    // ===== Agrupaciones (con DTO en lugar de Object[]) =====

    @Query("""
            SELECT new pe.com.carlosh.tallyapi.expense.dto.DailyTotalDTO(
                e.expenseDate, SUM(e.amount))
            FROM Expense e
            WHERE e.user.id = :userId AND e.active = true
              AND e.expenseDate >= :from
              AND e.expenseDate <= :to
            GROUP BY e.expenseDate
            ORDER BY e.expenseDate
            """)
    List<DailyTotalDTO> sumGroupedByDate(@Param("userId") Long userId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    @Query("""
            SELECT new pe.com.carlosh.tallyapi.expense.dto.CategoryTotalDTO(
                e.category.id, SUM(e.amount), COUNT(e.id))
            FROM Expense e
            WHERE e.user.id = :userId AND e.active = true
              AND e.category.id IN (:categoryIds)
            GROUP BY e.category.id
            """)
    List<CategoryTotalDTO> sumByCategoryIds(@Param("userId") Long userId,
                                            @Param("categoryIds") Collection<Long> categoryIds);

    // ===== Operaciones administrativas =====

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Expense e SET e.category = :newCategory
            WHERE e.category = :oldCategory AND e.user.id = :userId
            """)
    int reassignCategory(@Param("oldCategory") Category oldCategory,
                         @Param("newCategory") Category newCategory,
                         @Param("userId") Long userId);
}