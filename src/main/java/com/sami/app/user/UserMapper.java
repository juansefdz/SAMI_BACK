package com.sami.app.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.sami.app.user.dto.UserResponse;
import com.sami.app.user.dto.UserSummary;
import com.sami.app.user.dto.UserUpdateRequest;

@Mapper
public interface UserMapper {

    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "role", source = "account.role")
    @Mapping(target = "guest", source = "account.guest")
    UserResponse toResponse(User user);

    UserSummary toSummary(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    void update(@MappingTarget User user, UserUpdateRequest request);
}
