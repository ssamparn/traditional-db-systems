package com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.mapper;

import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.request.UserRequest;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response.UserResponse;
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
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setMobile(entity.getMobile());
        response.setEmail(entity.getEmail());

        return response;
    }

}
