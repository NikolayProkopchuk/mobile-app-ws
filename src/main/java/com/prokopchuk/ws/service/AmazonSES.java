package com.prokopchuk.ws.service;

import com.prokopchuk.ws.shared.dto.UserDto;

public interface AmazonSES {
    void verifyEmail(UserDto userDto);
    boolean sendPasswordResetRequest(String firstName, String email, String token);
}
