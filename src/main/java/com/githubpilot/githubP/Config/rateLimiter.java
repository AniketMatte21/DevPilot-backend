package com.githubpilot.githubP.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class rateLimiter {
    private final long delayMillis;

    public rateLimiter(@Value("${app.rate-limit.delay-ms:1000}") long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void pause() {

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Rate limiter interrupted",
                    e
            );
        }
    }
}
