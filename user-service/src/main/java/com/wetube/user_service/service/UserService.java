package com.wetube.user_service.service;

import com.wetube.user_service.dto.UserDto;
import com.wetube.user_service.entity.User;
import com.wetube.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return new UserDto(user.getUsername(), user.getEmail());
    }

    public Iterable<UserDto> all() {
        Iterable<User> allUsers = userRepository.findAll();
        ArrayList<UserDto> allUserDtos = new ArrayList<>();
        allUsers.forEach(user -> allUserDtos.addLast(new UserDto(user.getUsername(), user.getEmail())));
        return allUserDtos;
    }
}
