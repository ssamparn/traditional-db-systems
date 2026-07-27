package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.mapper.RelationMapper;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.response.UserResponse;
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
    private final RoleRepository roleRepository;
    private final RelationMapper relationMapper;

    public ResponseEntity<UserResponse> createUser(UserRequest request) {
        User user = relationMapper.toUserEntity(request);
        attachRolesById(user, request.getRoleIds());
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(relationMapper.toUserResponse(savedUser));
    }

    public ResponseEntity<UserResponse> updateUser(UserRequest request, Long id) {
        return userRepository.findById(id)
            .map(user -> {
                relationMapper.updateUserEntity(user, request);
                resetRoles(user);
                attachRolesById(user, request.getRoleIds());
                User savedUser = userRepository.save(user);
                return ResponseEntity.ok(relationMapper.toUserResponse(savedUser));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteUser(Long id) {
        return userRepository.findById(id)
            .map(user -> {
                resetRoles(user);
                userRepository.delete(user);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
            .map(relationMapper::toUserResponse)
            .toList();
    }

    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id)
            .map(relationMapper::toUserResponse);
    }

    private void attachRolesById(User user, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
        roles.forEach(user::addRole);
    }

    private void resetRoles(User user) {
        List<Role> existingRoles = List.copyOf(user.getRoles());
        existingRoles.forEach(user::removeRole);
    }
}

