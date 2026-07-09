package com.nntan041299.englishmasterservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void revoke(String token, Instant expiresAt) {
        revokedTokenRepository.save(new RevokedToken(token, expiresAt));
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String token) {
        return revokedTokenRepository.existsByToken(token);
    }

    // Runs on a configurable interval to purge tokens that have already expired naturally
    @Transactional
    @Scheduled(fixedRateString = "${token.blacklist.cleanup-interval-ms:3600000}")
    public void evictExpiredTokens() {
        revokedTokenRepository.deleteAllExpiredBefore(Instant.now());
    }
}
