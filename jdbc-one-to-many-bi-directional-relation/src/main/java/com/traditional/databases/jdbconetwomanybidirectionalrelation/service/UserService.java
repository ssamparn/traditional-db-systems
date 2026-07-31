package com.traditional.databases.jdbconetwomanybidirectionalrelation.service;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.mapper.UserMapper;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.exception.UserNotFoundException;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.RoleUserResponse;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public Mono<UserResponse> getUserById(Long userId) {
        return Mono.fromSupplier(() -> this.userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with Id: " + userId)))
                .map(userMapper::toUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<UserResponse> getAllUsers() {
        return Flux.fromIterable(this.userRepository.findAll())
                .map(userMapper::toUserResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<RoleUserResponse> getRoleUserInfo() {
        return Flux.fromIterable(this.userRepository.getJoinInformation())
                .subscribeOn(Schedulers.boundedElastic());
    }
}
