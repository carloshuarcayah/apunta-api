package pe.com.carlosh.tallyapi.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pe.com.carlosh.tallyapi.category.dto.CategoryRequestDTO;
import pe.com.carlosh.tallyapi.category.dto.CategoryResponseDTO;
import pe.com.carlosh.tallyapi.core.exception.AlreadyExistsException;
import pe.com.carlosh.tallyapi.core.exception.InvalidOperationException;
import pe.com.carlosh.tallyapi.core.exception.ResourceNotFoundException;
import pe.com.carlosh.tallyapi.user.User;
import pe.com.carlosh.tallyapi.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user1;
    private Category category1;
    private final Long USER_ID = 1L;
    private final Long CATEGORY_ID = 99L;

    @BeforeEach
    void setUp() {
        user1 = new User("prueba@gmail.com", "968574659", "usuarioprueba", "usuario123", "usuario", "prueba");
        ReflectionTestUtils.setField(user1, "id", USER_ID);
    }

    @Test
    @DisplayName("Create category - Ok")
    void create_Success() {
        CategoryRequestDTO req = new CategoryRequestDTO("Nueva Cat", "Desc");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user1));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndActiveTrue(USER_ID, "Nueva Cat")).thenReturn(false);

        Category savedCat = new Category(req.name(), req.description(), user1);
        ReflectionTestUtils.setField(savedCat, "id", 100L);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCat);

        CategoryResponseDTO response = categoryService.create(req, USER_ID);

        assertNotNull(response);
        assertEquals("Nueva Cat", response.name());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Create Category - Error: throws AlreadyExistsException")
    void create_ThrowsAlreadyExistsException() {
        CategoryRequestDTO req = new CategoryRequestDTO("Category 1", "Error Test");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user1));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndActiveTrue(USER_ID, req.name())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> {
            categoryService.create(req, USER_ID);
        });

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Update Category - Error: throws ResourceNotFoundException when User Is Not Owner")
    void update_ThrowsResourceNotFound() {
        CategoryRequestDTO req = new CategoryRequestDTO("Update Name", "Desc");
        Long wrongUserId = 2L;
        when(categoryRepository.findByIdAndUserIdAndActiveTrue(CATEGORY_ID, wrongUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.update(CATEGORY_ID, wrongUserId, req);
        });
    }

    @Test
    @DisplayName("Delete Category - Ok: deactivates when more than one active category exists")
    void delete_Success() {
        Category category = new Category("Cat A", "Desc", user1);
        ReflectionTestUtils.setField(category, "id", CATEGORY_ID);

        when(categoryRepository.findByIdAndUserIdAndActiveTrue(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(3L);

        categoryService.delete(CATEGORY_ID, USER_ID);

        assertFalse(category.isActive());
    }

    @Test
    @DisplayName("Delete Category - Error: throws InvalidOperationException when deleting the last category")
    void delete_ThrowsInvalidOperationException_WhenLastCategory() {
        Category category = new Category("Unica", "Desc", user1);
        ReflectionTestUtils.setField(category, "id", CATEGORY_ID);

        when(categoryRepository.findByIdAndUserIdAndActiveTrue(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(1L);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> categoryService.delete(CATEGORY_ID, USER_ID));

        assertEquals("Cannot delete the last category", ex.getMessage());
        assertTrue(category.isActive());
    }

    @Test
    @DisplayName("Update Category - Error: throws InvalidOperationException when category is system")
    void update_ThrowsInvalidOperationException_WhenSystemCategory() {
        Category systemCategory = new Category(Category.DEFAULT_SYSTEM_NAME, null, user1);
        systemCategory.setSystem(true);
        ReflectionTestUtils.setField(systemCategory, "id", CATEGORY_ID);

        when(categoryRepository.findByIdAndUserIdAndActiveTrue(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(systemCategory));

        CategoryRequestDTO req = new CategoryRequestDTO("Nuevo Nombre", "Desc");

        assertThrows(InvalidOperationException.class,
                () -> categoryService.update(CATEGORY_ID, USER_ID, req));

        assertEquals(Category.DEFAULT_SYSTEM_NAME, systemCategory.getName());
        verify(categoryRepository, never()).existsByUserIdAndNameIgnoreCaseAndActiveTrue(anyLong(), anyString());
    }

    @Test
    @DisplayName("Delete Category - Error: throws InvalidOperationException when category is system")
    void delete_ThrowsInvalidOperationException_WhenSystemCategory() {
        Category systemCategory = new Category(Category.DEFAULT_SYSTEM_NAME, null, user1);
        systemCategory.setSystem(true);
        ReflectionTestUtils.setField(systemCategory, "id", CATEGORY_ID);

        when(categoryRepository.findByIdAndUserIdAndActiveTrue(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(systemCategory));

        assertThrows(InvalidOperationException.class,
                () -> categoryService.delete(CATEGORY_ID, USER_ID));

        assertTrue(systemCategory.isActive());
        verify(categoryRepository, never()).countByUserIdAndActiveTrue(anyLong());
    }

    @Test
    @DisplayName("SetActive Category - Error: throws InvalidOperationException when deactivating system category")
    void setActive_ThrowsInvalidOperationException_WhenDeactivatingSystemCategory() {
        Category systemCategory = new Category(Category.DEFAULT_SYSTEM_NAME, null, user1);
        systemCategory.setSystem(true);
        ReflectionTestUtils.setField(systemCategory, "id", CATEGORY_ID);

        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(systemCategory));

        assertThrows(InvalidOperationException.class,
                () -> categoryService.setActive(CATEGORY_ID, USER_ID, false));

        assertTrue(systemCategory.isActive());
    }

    @Test
    @DisplayName("SetActive Category - Error: throws InvalidOperationException when activating system category")
    void setActive_ThrowsInvalidOperationException_WhenActivatingSystemCategory() {
        Category systemCategory = new Category(Category.DEFAULT_SYSTEM_NAME, null, user1);
        systemCategory.setSystem(true);
        ReflectionTestUtils.setField(systemCategory, "id", CATEGORY_ID);

        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(systemCategory));

        assertThrows(InvalidOperationException.class,
                () -> categoryService.setActive(CATEGORY_ID, USER_ID, true));
    }
}