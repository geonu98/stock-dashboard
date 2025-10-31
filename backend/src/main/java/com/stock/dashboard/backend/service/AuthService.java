package com.stock.dashboard.backend.service;

import com.stock.dashboard.backend.model.User;
import com.stock.dashboard.backend.model.payload.request.LoginRequest;
import com.stock.dashboard.backend.repository.UserRepository;
import com.stock.dashboard.backend.security.JwtTokenProvider;
import com.stock.dashboard.backend.security.model.CustomUserDetails;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 사용자 인증 메소드
     * @param loginRequest 클라이언트에서 들어온 이메일/비밀번호 정보
     * @return Optional<Authentication> 인증 성공 시 Authentication 객체 반환
     */
    public Optional<Authentication> authenticateUser(LoginRequest loginRequest) {
        log.info("[AuthService] Authenticating user: {}", loginRequest.getEmail());

        // 1️⃣ DB에서 사용자 존재 확인
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
        if (optionalUser.isEmpty()) {
            log.warn("[AuthService] User NOT found in DB: {}", loginRequest.getEmail());
            return Optional.empty();
        } else {
            log.info("[AuthService] User found in DB: {}", loginRequest.getEmail());
        }

        User user = optionalUser.get();

        // 2️⃣ DB 비밀번호와 입력 비밀번호 비교 로그 (디버깅용)
        log.info("[AuthService DEBUG] DB stored password: {}", user.getPassword());
        log.info("[AuthService DEBUG] Raw password from request: {}", loginRequest.getPassword());
        boolean matches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        log.info("[AuthService DEBUG] Does raw password match DB password? {}", matches);

        // 3️⃣ 비밀번호 확인
        if (!matches) {
            log.warn("[AuthService] Password mismatch for user: {}", loginRequest.getEmail());
            // ❌ 로그인 실패 시 BadCredentialsException 발생
            throw new BadCredentialsException("Invalid password");
        } else {
            log.info("[AuthService] Password matched for user: {}", loginRequest.getEmail());
        }

        // 4️⃣ Authentication 객체 생성 (Spring Security용)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user),
                null,
                new CustomUserDetails(user).getAuthorities()
        );

        log.info("[AuthService] Authentication object created for user: {}", loginRequest.getEmail());

        // 5️⃣ Optional로 Authentication 객체 반환
        return Optional.of(authentication);
    }

    /**
     * JWT 토큰 생성
     * @param userDetails CustomUserDetails 객체
     * @return JWT 토큰 문자열
     */
    public String generateToken(CustomUserDetails userDetails) {
        String token = jwtTokenProvider.generateToken(userDetails);
        log.info("[AuthService] Generated JWT token for user: {}", userDetails.getUsername());
        return token;
    }
}
