package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ResponseEntity<User> createUser(User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
    }

    public ResponseEntity<User> updateUser(User user, Long id) {
        return userRepository.findById(id)
            .map(existing -> {
                user.setId(existing.getId());
                return ResponseEntity.ok(userRepository.save(user));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}

