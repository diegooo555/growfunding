package com.g7.crowdfunding.user.application.query;

import com.g7.crowdfunding.user.application.port.out.UserRepository;
import com.g7.crowdfunding.user.domain.model.User;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserQueryService {

    private final UserRepository users;

    public UserQueryService(UserRepository users) {
        this.users = users;
    }

    public List<UserView> listUsers() {
        return users.findAll().stream()
                .sorted(Comparator.comparing(User::displayName))
                .map(UserQueryService::toView)
                .toList();
    }

    private static UserView toView(User user) {
        return new UserView(user.id(), user.displayName(), user.email(),
                user.roles().stream().map(Enum::name).collect(Collectors.toCollection(java.util.TreeSet::new)));
    }
}
