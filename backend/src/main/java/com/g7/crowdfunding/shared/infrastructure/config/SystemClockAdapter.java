package com.g7.crowdfunding.shared.infrastructure.config;

import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class SystemClockAdapter implements ClockPort {

    private final Clock clock = Clock.systemUTC();

    @Override
    public Instant now() {
        // Microsecond precision matches the database column precision.
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}
