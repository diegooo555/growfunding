package com.g7.crowdfunding.shared.application.port.out;

import java.time.Instant;

/**
 * Time source abstraction so tests can control "now".
 */
public interface ClockPort {

    Instant now();
}
