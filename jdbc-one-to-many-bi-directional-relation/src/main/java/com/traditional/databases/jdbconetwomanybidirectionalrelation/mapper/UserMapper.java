package com.traditional.databases.jdbconetwomanybidirectionalrelation.mapper;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUserEntity(UserRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());

        return user;
    }

    public UserResponse toUserResponse(User entity) {
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setMobile(entity.getMobile());
        response.setEmail(entity.getEmail());
        response.setRoleId(entity.getRole() == null ? null : entity.getRole().getId());

        return response;
    }

    public User updateUserEntity(User entity, UserRequest request) {
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setMobile(request.getMobile());
        entity.setEmail(request.getEmail());
        return entity;
    }

}
