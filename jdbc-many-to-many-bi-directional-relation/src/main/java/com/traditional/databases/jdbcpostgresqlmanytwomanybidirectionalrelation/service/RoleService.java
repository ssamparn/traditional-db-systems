package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.mapper.RelationMapper;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RelationMapper relationMapper;

    public ResponseEntity<RoleResponse> addRole(RoleRequest request) {
        Role role = relationMapper.toRoleEntity(request);
        attachUsersById(role, request.getUserIds());
        Role savedRole = roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(relationMapper.toRoleResponse(savedRole));
    }

    public ResponseEntity<RoleResponse> updateRole(Long id, RoleRequest request) {
        return roleRepository.findById(id)
            .map(role -> {
                relationMapper.updateRoleEntity(role, request);
                resetUsers(role);
                attachUsersById(role, request.getUserIds());
                Role savedRole = roleRepository.save(role);
                return ResponseEntity.ok(relationMapper.toRoleResponse(savedRole));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteRole(Long id) {
        return roleRepository.findById(id)
            .map(role -> {
                resetUsers(role);
                roleRepository.delete(role);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
            .map(relationMapper::toRoleResponse)
            .toList();
    }

    public Optional<RoleResponse> findById(Long id) {
        return roleRepository.findById(id)
            .map(relationMapper::toRoleResponse);
    }

    private void attachUsersById(Role role, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(role::addUser);
    }

    private void resetUsers(Role role) {
        List<User> existingUsers = List.copyOf(role.getUsers());
        existingUsers.forEach(role::removeUser);
    }
}


