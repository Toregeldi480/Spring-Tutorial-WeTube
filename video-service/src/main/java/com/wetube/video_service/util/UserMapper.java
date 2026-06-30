package com.wetube.video_service.util;

import com.wetube.video_service.dto.UserDto;
import com.wetube.video_service.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }
}
