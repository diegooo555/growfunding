package com.g7.crowdfunding.contribution.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param idempotencyKey client-generated UUID; repeating a request with the same key returns
 *                       the originally registered contribution instead of creating a new one
 */
public record ContributeRequest(
        @NotNull BigDecimal amount,
        @Size(min = 3, max = 3) String currency,
        boolean anonymous,
        @Size(max = 500) String message,
        @NotNull UUID idempotencyKey) {
}
