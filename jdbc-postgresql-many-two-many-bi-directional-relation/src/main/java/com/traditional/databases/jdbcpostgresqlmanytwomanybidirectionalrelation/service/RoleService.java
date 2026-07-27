package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository.RoleRepository;
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

    public ResponseEntity<Role> addRole(Role role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleRepository.save(role));
    }

    public ResponseEntity<Role> updateRole(Long id, Role role) {
        return roleRepository.findById(id)
            .map(existing -> {
                role.setId(existing.getId());
                return ResponseEntity.ok(roleRepository.save(role));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteRole(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }
}


