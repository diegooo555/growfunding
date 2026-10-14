package com.g7.crowdfunding.user.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.user.application.port.out.UserRepository;
import com.g7.crowdfunding.user.domain.model.Role;
import com.g7.crowdfunding.user.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository repository;

    UserPersistenceAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(UserPersistenceAdapter::toDomain).toList();
    }

    private static User toDomain(UserJpaEntity e) {
        Set<Role> roles = Arrays.stream(e.roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        return new User(e.id, e.displayName, e.email, roles);
    }
}
