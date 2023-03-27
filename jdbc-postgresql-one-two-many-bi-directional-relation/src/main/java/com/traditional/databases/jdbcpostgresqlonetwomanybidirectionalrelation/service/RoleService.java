package com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.mapper.RoleMapper;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.exception.RoleNotFoundException;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional
    public Mono<RoleResponse> createNewRole(final Mono<RoleRequest> roleMono) {
        return roleMono
                .map(roleMapper::toRoleEntity)
                .map(roleRepository::save)
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<RoleResponse> getAllRoles() {
        return Flux.fromIterable(this.roleRepository.findAll())
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<RoleResponse> getRoleById(Long roleId) {
        return Mono.fromSupplier(() -> this.roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with Id: " + roleId)))
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<RoleResponse> deleteRole(Long roleId) {
        return this.findById(roleId)
                .publishOn(Schedulers.boundedElastic())
                .map(role -> {
                    this.roleRepository.delete(role);
                    return role;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Role> findById(Long roleId) {
        return Mono.fromSupplier(() -> this.roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFoundException("Role not found with Id: " + roleId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
