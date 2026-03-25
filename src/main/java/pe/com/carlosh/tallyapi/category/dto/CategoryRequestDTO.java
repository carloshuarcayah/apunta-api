package pe.com.carlosh.tallyapi.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequestDTO(
        @NotBlank
        String name,
        String description
) {
}
