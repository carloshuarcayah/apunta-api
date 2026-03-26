package pe.com.carlosh.tallyapi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String identifier,
        @NotBlank String password
) {}