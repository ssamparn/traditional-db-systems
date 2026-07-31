package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.mapper;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.response.RoleResponse;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.response.RoleSummaryResponse;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.response.UserResponse;
import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.response.UserSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RelationMapper {

    public User toUserEntity(UserRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());
        return user;
    }

    public Role toRoleEntity(RoleRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        return role;
    }

    public void updateUserEntity(User user, UserRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());
    }

    public void updateRoleEntity(Role role, RoleRequest request) {
        role.setName(request.getName());
        role.setDescription(request.getDescription());
    }

    public UserResponse toUserResponse(User user) {
        List<RoleSummaryResponse> roles = user.getRoles().stream()
            .map(role -> new RoleSummaryResponse(role.getId(), role.getName(), role.getDescription()))
            .toList();

        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getMobile(),
            user.getEmail(),
            roles
        );
    }

    public RoleResponse toRoleResponse(Role role) {
        List<UserSummaryResponse> users = role.getUsers().stream()
            .map(user -> new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail()))
            .toList();

        return new RoleResponse(
            role.getId(),
            role.getName(),
            role.getDescription(),
            users
        );
    }
}

