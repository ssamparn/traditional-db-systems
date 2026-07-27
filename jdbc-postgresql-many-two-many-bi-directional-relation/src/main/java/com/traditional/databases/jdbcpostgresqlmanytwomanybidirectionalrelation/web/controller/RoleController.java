package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/role/create")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return  roleService.addRole(role);
    }

    @DeleteMapping("/role/delete/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }

    @GetMapping("/role/details/{id}")
    public ResponseEntity<Role> getRole(@PathVariable Long id) {
        return roleService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/role/all")
    public List<Role> getRoles() {
        return roleService.findAll();
    }

    @PutMapping("/role/update/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }
}
