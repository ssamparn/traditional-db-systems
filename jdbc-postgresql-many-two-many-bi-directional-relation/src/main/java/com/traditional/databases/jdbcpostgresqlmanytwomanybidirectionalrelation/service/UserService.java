package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ResponseEntity<Object> createUser(User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
    }

    public ResponseEntity<Object> updateUser(User user, Long id) {
        return userRepository.findById(id)
            .<ResponseEntity<Object>>map(existing -> {
                user.setId(existing.getId());
                return ResponseEntity.ok(userRepository.save(user));
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User not found with id: " + id));
    }

    public ResponseEntity<Object> deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}

