package com.stock.dashboard.backend.cache;

import com.stock.dashboard.backend.event.OnUserLogoutSuccessEvent;
import com.stock.dashboard.backend.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LoggedOutJwtTokenCache {

    private final ExpiringMap<String, OnUserLogoutSuccessEvent> tokenEventMap;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public LoggedOutJwtTokenCache(@Value("${app.cache.logoutToken.maxSize}") int maxSize, JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.tokenEventMap = ExpiringMap.builder()
                .variableExpiration()
                .maxSize(maxSize)
                .build();
    }

    /**
     * 토큰 만료일을 기반으로 토큰의 TTL(Time-To-Live)을 계산합니다.
     * @param tokenExpiryDate 토큰 만료일 (Date 객체)
     * @return 캐시에서 토큰을 유지할 시간 (초 단위)
     */
    private long getTTLForToken(Date tokenExpiryDate) {
        long currentMillis = Instant.now().toEpochMilli();
        long expiryMillis = tokenExpiryDate.getTime();

        // 현재 시간보다 만료 시간이 지난 경우 1초 TTL을 반환 (즉시 만료되도록)
        if (expiryMillis < currentMillis) {
            return 1;
        }

        // 만료 시간까지 남은 시간을 초 단위로 계산
        return TimeUnit.MILLISECONDS.toSeconds(expiryMillis - currentMillis);
    }

    /**
     * 로그아웃 이벤트를 캐시에 기록합니다.
     * @param event 로그아웃 성공 이벤트
     */
    public void markLogoutEventForToken(OnUserLogoutSuccessEvent event) {
        String token = event.getToken();
        if (tokenEventMap.containsKey(token)) {
            log.info("Log out token for user [{}] is already present in the cache", event.getUserEmail());
        } else {
            Date tokenExpiryDate = tokenProvider.getTokenExpiryFromJWT(token);
            long ttlForToken = getTTLForToken(tokenExpiryDate);

            // 토큰 만료일이 현재 시간보다 미래인 경우에만 캐시에 저장
            if (ttlForToken > 1) {
                log.info("Logout token cache set for [{}] with a TTL of [{}] seconds. Token is due expiry at [{}]",
                        event.getUserEmail(), ttlForToken, tokenExpiryDate);
                tokenEventMap.put(token, event, ttlForToken, TimeUnit.SECONDS);
            } else {
                log.warn("Token for user [{}] has already expired and will not be cached.", event.getUserEmail());
            }
        }
    }

    /**
     * JWT가 캐시에 저장된 로그아웃된 토큰인지 확인합니다.
     * @param authToken JWT
     * @return 로그아웃된 토큰이면 true
     */
    public boolean isTokenLoggedOut(String authToken) {
        return tokenEventMap.containsKey(authToken);
    }

    public OnUserLogoutSuccessEvent getLogoutEventForToken(String authToken) {
        return tokenEventMap.get(authToken);
    }
}
