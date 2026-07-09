package com.nntan041299.englishmasterservice.auth.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived, in-memory store for the OAuth2 "state" values issued by
 * {@code GoogleOauth2Impl#buildAuthUrl()} to protect the login redirect
 * against CSRF. State is single-use: {@link #validateAndConsume(String)}
 * removes it so it can't be replayed.
 *
 * <p>This is intentionally in-memory rather than DB/Redis-backed: state is
 * only needed for the few minutes between redirecting the user to Google and
 * them coming back with a code. Note this means state won't survive an app
 * restart and won't be shared if this service is ever scaled to multiple
 * instances (a sticky session or shared cache would be needed then).
 */
@Component
public class OAuthStateStore {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final Map<String, Instant> states = new ConcurrentHashMap<>();

    public void save(String state) {
        states.put(state, Instant.now().plus(STATE_TTL));
    }

    /** Returns true if the state was known and not yet expired; consumes it either way. */
    public boolean validateAndConsume(String state) {
        Instant expiresAt = states.remove(state);
        return expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    @Scheduled(fixedRate = 60_000)
    public void evictExpired() {
        Instant now = Instant.now();
        states.values().removeIf(expiresAt -> expiresAt.isBefore(now));
    }
}
