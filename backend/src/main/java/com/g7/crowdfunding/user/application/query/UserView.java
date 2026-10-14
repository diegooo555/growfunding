package com.g7.crowdfunding.user.application.query;

import java.util.Set;
import java.util.UUID;

public record UserView(UUID id, String displayName, String email, Set<String> roles) {
}
