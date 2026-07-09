package com.nntan041299.englishmasterservice.auth.mapper;

import com.nntan041299.englishmasterservice.auth.dto.UserResponse;
import com.nntan041299.englishmasterservice.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
