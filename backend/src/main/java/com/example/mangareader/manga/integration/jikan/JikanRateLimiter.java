package com.example.mangareader.manga.integration.jikan;

import com.example.mangareader.common.exception.AppException;
import com.example.mangareader.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Client khusus Jikan dengan limiter internal konservatif dan retry kecil untuk error sementara.
 * - 429 → UPSTREAM_RATE_LIMITED tanpa retry agresif.
 * - 5xx/timeout → retry terbatas dengan backoff eksponensial sederhana, lalu UPSTREAM_UNAVAILABLE.
 * - 404 → NOT_FOUND (tanpa retry).
 * - Response error tidak pernah di-cache sebagai data valid (penanganan ada di service layer).
 */
@Component
public class JikanRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(JikanRateLimiter.class);

    private final long minIntervalMillis;
    private final int maxRetryAttempts;
    private final Object lock = new Object();
    private long lastRequestAt = 0;

    public JikanRateLimiter(@Value("${jikan.max-request-per-minute}") int maxPerMinute,
                            @Value("${jikan.retry-max-attempts}") int maxRetryAttempts) {
        this.minIntervalMillis = maxPerMinute <= 0 ? 2000 : Math.max(1000L / maxPerMinute, 60_000L / maxPerMinute);
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
    }

    void acquire() {
        long waitMillis;
        synchronized (lock) {
            long now = System.currentTimeMillis();
            waitMillis = lastRequestAt + minIntervalMillis - now;
            if (waitMillis <= 0) {
                lastRequestAt = now;
            } else {
                lastRequestAt = now + waitMillis;
            }
        }
        if (waitMillis > 0) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AppException(ErrorCode.UPSTREAM_UNAVAILABLE, "Layanan metadata sedang sibuk");
            }
        }
    }

    public <T> T execute(String description, java.util.function.Supplier<T> call) {
        int attempt = 0;
        while (true) {
            acquire();
            try {
                return call.get();
            } catch (HttpClientErrorException.NotFound ex) {
                throw AppException.notFound("Data manga tidak ditemukan pada penyedia metadata");
            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode().value() == 429) {
                    log.warn("Jikan rate limited (429) for {}", description);
                    throw new AppException(ErrorCode.UPSTREAM_RATE_LIMITED,
                            "Penyedia metadata sedang membatasi request. Coba lagi beberapa saat.");
                }
                // 400 dsb: jangan retry agresif.
                log.warn("Jikan client error {} for {}: {}", ex.getStatusCode(), description, ex.getMessage());
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Permintaan ke penyedia metadata ditolak");
            } catch (HttpServerErrorException | ResourceAccessException ex) {
                attempt++;
                if (attempt > maxRetryAttempts) {
                    log.error("Jikan unavailable for {} after {} attempts: {}", description, attempt, ex.getMessage());
                    throw new AppException(ErrorCode.UPSTREAM_UNAVAILABLE,
                            "Penyedia metadata tidak dapat dihubungi saat ini. Coba lagi nanti.");
                }
                long backoff = 500L * (1L << (attempt - 1)); // 500ms, 1s, ...
                log.warn("Jikan transient error for {}, retry {}/{} in {}ms", description, attempt, maxRetryAttempts, backoff);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AppException(ErrorCode.UPSTREAM_UNAVAILABLE, "Penyedia metadata tidak dapat dihubungi");
                }
            }
        }
    }
}
