package com.prokopchuk.ws.service;

import com.prokopchuk.ws.shared.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto user);
}
