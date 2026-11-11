package com.courtbook.identity.service;

import com.courtbook.identity.dto.TokenResponse;
import com.courtbook.identity.entity.OtpCode;
import com.courtbook.identity.entity.Role;
import com.courtbook.identity.entity.User;
import com.courtbook.identity.event.OtpRequestedEvent;
import com.courtbook.identity.repository.OtpCodeRepository;
import com.courtbook.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String OTP_REQUESTED_TOPIC = "otp-requested";

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void requestOtp(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone is required");
        }
        otpCodeRepository.findByPhoneAndUsedFalse(phone).forEach(old -> old.setUsed(true));

        OtpCode otp = new OtpCode();
        otp.setPhone(phone);
        otp.setCode(String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000)));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpCodeRepository.save(otp);

        kafkaTemplate.send(OTP_REQUESTED_TOPIC, phone, new OtpRequestedEvent(phone, otp.getCode()));
    }

    @Transactional
    public TokenResponse verifyOtp(String phone, String code) {
        OtpCode otp = otpCodeRepository.findFirstByPhoneAndUsedFalseOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Code not found"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Code expired");
        }
        if (!otp.getCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong code");
        }
        otp.setUsed(true);

        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setRole(Role.CLIENT);
            return userRepository.save(newUser);
        });

        return toTokenResponse(user);
    }

    @Transactional
    public TokenResponse registerOwner(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password (min 6 chars) are required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.OWNER);
        userRepository.save(user);

        return toTokenResponse(user);
    }

    public TokenResponse loginOwner(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getRole() == Role.OWNER)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));

        return toTokenResponse(user);
    }

    private TokenResponse toTokenResponse(User user) {
        return new TokenResponse(user.getId(), user.getRole(), jwtService.createToken(user));
    }
}
