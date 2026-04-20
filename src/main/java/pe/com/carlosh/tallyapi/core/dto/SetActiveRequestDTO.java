package pe.com.carlosh.tallyapi.core.dto;

import jakarta.validation.constraints.NotNull;

public record SetActiveRequestDTO(
        @NotNull Boolean active
) {}