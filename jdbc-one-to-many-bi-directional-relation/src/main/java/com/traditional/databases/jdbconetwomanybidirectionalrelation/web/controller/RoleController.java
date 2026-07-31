package com.traditional.databases.jdbconetwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.service.RoleService;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/role/create")
    public Mono<ResponseEntity<RoleResponse>> createRole(@RequestBody RoleRequest request) {
        return roleService.createRole(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/role/get/all")
    public Flux<RoleResponse> getRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/role/get/{roleId}")
    public Mono<ResponseEntity<RoleResponse>> getRole(@PathVariable(name = "roleId") Long roleId) {
        return roleService.getRoleById(roleId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @PutMapping("/role/update/{roleId}")
    public Mono<ResponseEntity<RoleResponse>> updateRole(@PathVariable(name = "roleId") Long roleId,
                                                         @RequestBody RoleRequest request) {
        return roleService.updateRole(roleId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/role/delete/{roleId}")
    public Mono<ResponseEntity<RoleResponse>> deleteRole(@PathVariable(name = "roleId") Long roleId) {
        return roleService.deleteRole(roleId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}