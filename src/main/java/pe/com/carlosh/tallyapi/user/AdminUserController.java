package pe.com.carlosh.tallyapi.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.carlosh.tallyapi.core.dto.SetActiveRequestDTO;
import pe.com.carlosh.tallyapi.user.dto.ChangeRoleRequestDTO;
import pe.com.carlosh.tallyapi.user.dto.UserResponseDTO;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(adminUserService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.findById(id));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActive(
            @PathVariable Long id,
            @Valid @RequestBody SetActiveRequestDTO req) {
        adminUserService.setActive(id, req.active());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequestDTO req) {
        adminUserService.changeRole(id, req.role());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resend-verification")
    public ResponseEntity<Void> resendVerification(@PathVariable Long id) {
        adminUserService.resendVerification(id);
        return ResponseEntity.noContent().build();
    }
}