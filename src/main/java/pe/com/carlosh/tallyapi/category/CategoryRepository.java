package pe.com.carlosh.tallyapi.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.carlosh.tallyapi.user.User;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByActiveTrue(Pageable pageable);
    Page<Category> findByActiveFalse(Pageable pageable);
    Page<Category> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    Optional<Category> findByIdAndUserIdAndActiveTrue(Long id,Long userId);
    Optional<Category> findByIdAndUserId(Long id,Long userId);


    Page<Category> findByUserIdAndNameContainingIgnoreCaseAndActiveTrue(Long userId, String name, Pageable pageable);
    Page<Category> findByUserIdAndActiveTrue(Long userId, Pageable pageable);
    boolean existsByUserIdAndNameIgnoreCaseAndActiveTrue(Long userId, String name);
}