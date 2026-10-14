package com.g7.crowdfunding.user.infrastructure.adapter.in.web;

import com.g7.crowdfunding.user.application.query.UserQueryService;
import com.g7.crowdfunding.user.application.query.UserView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
class UserController {

    private final UserQueryService users;

    UserController(UserQueryService users) {
        this.users = users;
    }

    /**
     * Lists the demo users so the frontend can "log in as" one of them.
     */
    @GetMapping
    List<UserView> list() {
        return users.listUsers();
    }
}
