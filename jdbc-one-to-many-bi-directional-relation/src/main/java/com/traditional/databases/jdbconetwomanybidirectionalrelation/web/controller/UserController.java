package com.traditional.databases.jdbconetwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.service.UserService;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.RoleUserResponse;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @PostMapping("/user/create/role/{roleId}")
    public Mono<ResponseEntity<UserResponse>> createUser(@PathVariable Long roleId,
                                                         @RequestBody UserRequest request) {
        return userService.createUser(roleId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

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

    @PutMapping("/user/update/{userId}")
    public Mono<ResponseEntity<UserResponse>> updateUser(@PathVariable Long userId,
                                                         @RequestBody UserRequest request) {
        return userService.updateUser(userId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @PutMapping("/user/reassign/{userId}/role/{roleId}")
    public Mono<ResponseEntity<UserResponse>> reassignRole(@PathVariable Long userId,
                                                           @PathVariable Long roleId) {
        return userService.reassignUserRole(userId, roleId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/user/delete/{userId}")
    public Mono<ResponseEntity<UserResponse>> deleteUser(@PathVariable Long userId) {
        return userService.deleteUserById(userId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}
