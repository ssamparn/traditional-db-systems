package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.service.UserService;
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
    private final UserRepository userRepository;

    @PostMapping("/user/create")
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/user/details/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @GetMapping("/user/all")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/user/update/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(user, id);
    }

    @DeleteMapping("user/delete/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

}
