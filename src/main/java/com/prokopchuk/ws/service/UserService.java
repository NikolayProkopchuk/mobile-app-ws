package com.prokopchuk.ws.service;

import com.prokopchuk.ws.shared.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserDto createUser(UserDto user);
    UserDto getUserByEmail(String email);

    UserDto getUserById(String id);

    UserDto updateUser(String userId, UserDto userDto);
}
