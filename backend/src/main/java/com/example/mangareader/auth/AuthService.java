package com.example.mangareader.auth;

import com.example.mangareader.common.exception.AppException;
import com.example.mangareader.common.exception.ErrorCode;
import com.example.mangareader.user.User;
import com.example.mangareader.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public LoginResponse register(AuthRequests.RegisterRequest req) {
        String email = normalizeEmail(req.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw AppException.conflict("Email sudah terdaftar");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        String displayName = (req.displayName() == null || req.displayName().isBlank())
                ? email.substring(0, Math.min(email.indexOf('@'), 100))
                : req.displayName().trim();
        user.setDisplayName(displayName);
        user.setRole("USER");
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Race pada unique constraint email.
            throw AppException.conflict("Email sudah terdaftar");
        }
        String token = tokenProvider.generate(user.getId(), user.getEmail(), user.getRole());
        return new LoginResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(), token);
    }

    public LoginResponse login(AuthRequests.LoginRequest req) {
        String email = normalizeEmail(req.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Email atau password salah"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Email atau password salah");
        }
        String token = tokenProvider.generate(user.getId(), user.getEmail(), user.getRole());
        return new LoginResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(), token);
    }

    static String normalizeEmail(String email) {
        if (email == null) throw AppException.validation("Email wajib diisi");
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
