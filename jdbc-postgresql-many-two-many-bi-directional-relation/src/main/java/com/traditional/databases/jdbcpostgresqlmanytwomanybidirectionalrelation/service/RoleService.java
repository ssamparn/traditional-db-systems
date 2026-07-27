package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public ResponseEntity<Object> addRole(Role role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleRepository.save(role));
    }

    public ResponseEntity<Object> updateRole(Long id, Role role) {
        return roleRepository.findById(id)
            .<ResponseEntity<Object>>map(existing -> {
                role.setId(existing.getId());
                return ResponseEntity.ok(roleRepository.save(role));
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Role not found with id: " + id));
    }

    public ResponseEntity<Object> deleteRole(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with id: " + id);
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }
}


