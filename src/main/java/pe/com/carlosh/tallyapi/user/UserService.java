package pe.com.carlosh.tallyapi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.carlosh.tallyapi.auth.VerificationToken;
import pe.com.carlosh.tallyapi.auth.VerificationTokenRepository;
import pe.com.carlosh.tallyapi.budget.BudgetRepository;
import pe.com.carlosh.tallyapi.budget.dto.BudgetStatsDTO;
import pe.com.carlosh.tallyapi.category.Category;
import pe.com.carlosh.tallyapi.category.CategoryRepository;
import pe.com.carlosh.tallyapi.category.dto.CategoryStatsDTO;
import pe.com.carlosh.tallyapi.core.exception.AlreadyExistsException;
import pe.com.carlosh.tallyapi.core.exception.InvalidOperationException;
import pe.com.carlosh.tallyapi.core.exception.PasswordMismatchException;
import pe.com.carlosh.tallyapi.core.exception.ResourceNotFoundException;
import pe.com.carlosh.tallyapi.expense.ExpenseRepository;
import pe.com.carlosh.tallyapi.expense.dto.ExpenseStatsDTO;
import pe.com.carlosh.tallyapi.notification.EmailService;
import pe.com.carlosh.tallyapi.security.JwtService;
import pe.com.carlosh.tallyapi.tier.Tier;
import pe.com.carlosh.tallyapi.tier.TierName;
import pe.com.carlosh.tallyapi.tier.TierRepository;
import pe.com.carlosh.tallyapi.user.dto.TierInfoDTO;
import pe.com.carlosh.tallyapi.user.dto.*;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final TierRepository tierRepository;

    @Transactional
    public void register(UserRequestDTO req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new AlreadyExistsException("email", "El email ya está registrado");
        }

        if(!req.password1().equals(req.password2())){
            throw new PasswordMismatchException("Las contraseñas no coinciden");
        }

        Tier freeTier = tierRepository.findByName(TierName.FREE)
                .orElseThrow(() -> new ResourceNotFoundException("Default tier not found"));

        User user = UserMapper.toEntity(req, passwordEncoder.encode(req.password1()));
        user.assignTier(freeTier);
        userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
    }

    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public LoginResponseDTO login(LoginRequestDTO req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no existe"));

        // Si no ha verificado su correo, no puede ingresar.
        if (!user.isEmailVerified()) {
            throw new InvalidOperationException("Debes verificar tu correo electrónico antes de poder iniciar sesión.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        String jwtToken = jwtService.generateToken(user);
        return new LoginResponseDTO(jwtToken, user.getName(), user.isOnboardingCompleted());

    }

    @Transactional
    public void completeOnboarding(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.completeOnboarding();
        userRepository.save(user);
    }

    public UserStatsDTO getStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Tier tier = user.getTier();

        BigDecimal totalSpent = expenseRepository.sumTotalByUserId(userId);
        BigDecimal thisMonth = expenseRepository.sumTotalByUserIdThisMonth(userId);
        BigDecimal lastMonth = expenseRepository.sumTotalByUserIdLastMonth(userId);
        BigDecimal vsLastMonthPercent = percentChange(thisMonth, lastMonth);
        long expenseCount = expenseRepository.countByUserId(userId);
        long thisMonthCount = expenseRepository.countByUserIdThisMonth(userId);


        long budgetCount = budgetRepository.countByUserIdAndActiveTrue(userId);
        long exceededCount = budgetRepository.countExceededByUserId(userId);

        long categoryCount = categoryRepository.countByUserIdAndActiveTrueAndPredefinedFalse(userId);

        String topName = null;
        Long topCategoryId = null;
        BigDecimal topThisMonth = BigDecimal.ZERO;

        List<Category> categories = categoryRepository.findByUserIdAndActiveTrue(userId);
        for (Category cat : categories) {
            BigDecimal spent = expenseRepository.sumTotalByUserIdAndCategoryIdThisMonth(userId, cat.getId());
            if (spent.compareTo(topThisMonth) > 0) {
                topThisMonth = spent;
                topName = cat.getName();
                topCategoryId = cat.getId();
            }
        }

        BigDecimal topSpent = BigDecimal.ZERO;
        long topThisMonthCount = 0;
        if (topCategoryId != null) {
            topSpent = expenseRepository.sumTotalByUserIdAndCategoryId(userId, topCategoryId);
            topThisMonthCount = expenseRepository.countByUserIdAndCategoryIdThisMonth(userId, topCategoryId);
        }

        TierInfoDTO tierInfo = new TierInfoDTO(tier.getName().name(), tier.getMaxCategories(), tier.getMaxBudgets());

        return new UserStatsDTO(
                tierInfo,
                new BudgetStatsDTO(budgetCount, tier.getMaxBudgets(), exceededCount),
                new ExpenseStatsDTO(totalSpent, thisMonth, lastMonth, vsLastMonthPercent, expenseCount, thisMonthCount),
                new CategoryStatsDTO(categoryCount, tier.getMaxCategories(), topName, topSpent, topThisMonth, topThisMonthCount)
        );
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Enlace de verificación inválido o inexistente."));

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new InvalidOperationException("El enlace de verificación ha expirado. Por favor, regístrate nuevamente.");
        }

        // Activamos el correo del usuario
        User user = verificationToken.getUser();
        user.verifyEmail();
        userRepository.save(user);

        //creamos la categoria sin categoria por defecto
        Category category = new Category(Category.DEFAULT_SYSTEM_NAME, null, user);
        category.setPredefined(true);
        categoryRepository.save(category);

        // Limpiamos el token de la BD porque ya se usó
        tokenRepository.delete(verificationToken);
    }

    private BigDecimal percentChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.signum() == 0) return null;
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, java.math.RoundingMode.HALF_UP);
    }
}