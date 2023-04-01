package com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.service.UserService;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response.RoleUserResponse;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/user/get/{userId}")
    public Mono<ResponseEntity<UserResponse>> getUser(@PathVariable(name = "userId") Long userId) {
        return userService.getUserById(userId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/user/get/all")
    public Flux<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/role/user/info")
    public Flux<RoleUserResponse> getRoleUserJoinedResponse() {
        return userService.getRoleUserInfo();
    }
}
