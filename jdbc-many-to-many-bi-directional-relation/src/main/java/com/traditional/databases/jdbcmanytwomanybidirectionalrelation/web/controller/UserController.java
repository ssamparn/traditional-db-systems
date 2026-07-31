package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service.UserService;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user/create")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @GetMapping("/user/details/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/all")
    public List<UserResponse> getUsers() {
        return userService.findAll();
    }

    @PutMapping("/user/update/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        return userService.updateUser(userRequest, id);
    }

    @DeleteMapping("user/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

}
