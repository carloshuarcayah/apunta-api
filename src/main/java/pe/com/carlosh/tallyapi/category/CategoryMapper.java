package pe.com.carlosh.tallyapi.category;

import pe.com.carlosh.tallyapi.category.dto.CategoryRequestDTO;
import pe.com.carlosh.tallyapi.category.dto.CategoryResponseDTO;
import pe.com.carlosh.tallyapi.user.User;

public class CategoryMapper {
    public static Category toEntity(CategoryRequestDTO req, User user){
        return new Category(req.name(), req.description(),user);
    }

    public static CategoryResponseDTO toResponse(Category category){
        return new CategoryResponseDTO(category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getUser().getId()
        );
    }
}
