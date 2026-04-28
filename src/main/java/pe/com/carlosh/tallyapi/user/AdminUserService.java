package pe.com.carlosh.tallyapi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.carlosh.tallyapi.auth.VerificationToken;
import pe.com.carlosh.tallyapi.auth.VerificationTokenRepository;
import pe.com.carlosh.tallyapi.core.exception.InvalidOperationException;
import pe.com.carlosh.tallyapi.core.exception.ResourceNotFoundException;
import pe.com.carlosh.tallyapi.notification.EmailService;
import pe.com.carlosh.tallyapi.user.dto.UserResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        user.changeActive(active);
        userRepository.save(user);
    }

    @Transactional
    public void changeRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        user.changeRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerification(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        if (user.isEmailVerified()) {
            throw new InvalidOperationException("El correo ya fue verificado");
        }
        tokenRepository.deleteByUser(user);
        VerificationToken token = new VerificationToken(user);
        tokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), token.getToken());
    }
}