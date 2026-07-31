package com.traditional.databases.jdbconetwomanybidirectionalrelation.service;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.mapper.UserMapper;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.RoleUserResponse;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public Mono<UserResponse> createUser(Long roleId, UserRequest request) {
        return Mono.fromSupplier(() -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with Id: " + roleId)))
                .doOnNext(ignored -> UserRequestValidator.validate(request))
                .map(role -> {
                    User user = userMapper.toUserEntity(request);
                    user.setRole(role);
                    return user;
                })
                .map(userRepository::save)
                .map(userMapper::toUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UserResponse> getUserById(Long userId) {
        return findById(userId)
                .map(userMapper::toUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<UserResponse> getAllUsers() {
        return Flux.fromIterable(this.userRepository.findAll())
                .map(userMapper::toUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<RoleUserResponse> getRoleUserInfo() {
        return Flux.fromIterable(userRepository.getJoinInformation())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<UserResponse> updateUser(Long userId, UserRequest request) {
        return findById(userId)
                .doOnNext(ignored -> UserRequestValidator.validate(request))
                .map(user -> userMapper.updateUserEntity(user, request))
                .map(userRepository::save)
                .map(userMapper::toUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<UserResponse> reassignUserRole(Long userId, Long roleId) {
        return findById(userId)
                .zipWith(Mono.fromSupplier(() -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with Id: " + roleId))))
                .map(tuple -> {
                    User user = tuple.getT1();
                    user.setRole(tuple.getT2());
                    return user;
                })
                .map(userRepository::save)
                .map(userMapper::toUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<UserResponse> deleteUserById(Long userId) {
        return findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return user;
                })
                .map(userMapper::toUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> findById(Long userId) {
        return Mono.fromSupplier(() -> userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with Id: " + userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
