package com.prokopchuk.ws.service.impl;

import com.prokopchuk.ws.UserRepository;
import com.prokopchuk.ws.io.entity.UserEntity;
import com.prokopchuk.ws.service.UserService;
import com.prokopchuk.ws.shared.Utils;
import com.prokopchuk.ws.shared.dto.UserDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    Utils utils;

    @Override
    public UserDto createUser(UserDto user) {

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Record already exists");
        }
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(user, userEntity);

        String publicUserId = utils.generatedUserId(30);
        userEntity.setEncryptedPassword("test");
        userEntity.setUserId(publicUserId);

        UserEntity storedUserDetails = userRepository.save(userEntity);

        UserDto returnValue = new UserDto();
        BeanUtils.copyProperties(storedUserDetails, returnValue);
        return returnValue;
    }
}
