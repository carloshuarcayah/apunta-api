package pe.com.carlosh.tallyapi.category.dto;

public record CategoryResponseDTO(
        Long id,
        String name,
        String description,
        Boolean active
) {
}
