package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service.RoleService;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.RoleResponse;
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
    public ResponseEntity<RoleResponse> createRole(@RequestBody RoleRequest roleRequest) {
        return roleService.addRole(roleRequest);
    }

    @DeleteMapping("/role/delete/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }

    @GetMapping("/role/details/{id}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable Long id) {
        return roleService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/role/all")
    public List<RoleResponse> getRoles() {
        return roleService.findAll();
    }

    @PutMapping("/role/update/{id}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @RequestBody RoleRequest roleRequest) {
        return roleService.updateRole(id, roleRequest);
    }
}
