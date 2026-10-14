package com.g7.crowdfunding.user.application.port.out;

import com.g7.crowdfunding.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    List<User> findAll();
}
