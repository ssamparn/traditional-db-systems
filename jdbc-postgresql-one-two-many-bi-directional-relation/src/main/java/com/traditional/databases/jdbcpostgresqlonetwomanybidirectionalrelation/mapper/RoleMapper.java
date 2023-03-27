package com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.mapper;

import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response.RoleResponse;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final UserMapper userMapper;

    public Role toRoleEntity(RoleRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        List<User> users = createUsers(request.getUsers());
        role.setUsers(users);
        return role;
    }

    private List<User> createUsers(List<UserRequest> users) {
        return users.stream()
                .map(userMapper::toUserEntity)
                .collect(Collectors.toList());
    }

    public RoleResponse toRoleResponse(Role entity) {
        RoleResponse response = new RoleResponse();
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        List<UserResponse> users = createUserResponse(entity.getUsers());
        response.setUsers(users);
        return response;
    }

    private List<UserResponse> createUserResponse(List<User> users) {
        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }
}
