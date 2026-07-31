package com.traditional.databases.jdbconetwomanybidirectionalrelation.service;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.mapper.RoleMapper;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.RoleResponse;
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

    @Transactional
    public Mono<RoleResponse> createRole(RoleRequest request) {
        return Mono.just(request)
                .doOnNext(RoleRequestValidator::validate)
                .map(roleMapper::toRoleEntity)
                .map(roleRepository::save)
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<RoleResponse> getAllRoles() {
        return Flux.fromIterable(roleRepository.findAllWithUsers())
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<RoleResponse> getRoleById(Long roleId) {
        return findByIdWithUsers(roleId)
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<RoleResponse> updateRole(Long roleId, RoleRequest request) {
        return findByIdWithUsers(roleId)
                .doOnNext(ignored -> RoleRequestValidator.validate(request))
                .map(role -> roleMapper.updateRoleEntity(role, request))
                .map(roleRepository::save)
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<RoleResponse> deleteRole(Long roleId) {
        return findByIdWithUsers(roleId)
                .map(role -> {
                    roleRepository.delete(role);
                    return role;
                })
                .map(roleMapper::toRoleResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Role> findById(Long roleId) {
        return Mono.fromSupplier(() -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with Id: " + roleId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Role> findByIdWithUsers(Long roleId) {
        return Mono.fromSupplier(() -> roleRepository.findByIdWithUsers(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with Id: " + roleId)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
