package com.traditional.databases.jdbconetwomanybidirectionalrelation.mapper;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.RoleResponse;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.UserResponse;
import jakarta.persistence.Persistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final UserMapper userMapper;

    public Role toRoleEntity(RoleRequest request) {
        Role role = new Role();
        applyRequest(role, request);
        return role;
    }

    public Role updateRoleEntity(Role role, RoleRequest request) {
        applyRequest(role, request);
        return role;
    }

    private void applyRequest(Role role, RoleRequest request) {
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // Replace users deterministically so orphanRemoval handles removed rows.
        List<User> existingUsers = new ArrayList<>(role.getUsers());
        existingUsers.forEach(role::removeUser);

        List<User> users = createUsers(request.getUsers());
        users.forEach(role::addUser);
    }

    private List<User> createUsers(List<UserRequest> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(userMapper::toUserEntity)
                .collect(Collectors.toList());
    }

    public RoleResponse toRoleResponse(Role entity) {
        RoleResponse response = new RoleResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        List<UserResponse> users = isUsersLoaded(entity)
                ? createUserResponse(entity.getUsers())
                : Collections.emptyList();
        response.setUsers(users);
        return response;
    }

    private boolean isUsersLoaded(Role entity) {
        return Persistence.getPersistenceUtil().isLoaded(entity, "users");
    }

    private List<UserResponse> createUserResponse(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }
}
