package com.prokopchuk.ws.service.impl;

import com.prokopchuk.ws.exceptions.UserServiceException;
import com.prokopchuk.ws.io.entity.PasswordResetTokenEntity;
import com.prokopchuk.ws.io.repositories.PasswordResetTokenRepository;
import com.prokopchuk.ws.io.repositories.UserRepository;
import com.prokopchuk.ws.io.entity.UserEntity;
import com.prokopchuk.ws.service.AmazonSES;
import com.prokopchuk.ws.service.UserService;
import com.prokopchuk.ws.shared.Utils;
import com.prokopchuk.ws.shared.dto.UserDto;
import com.prokopchuk.ws.ui.model.response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Utils utils;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AmazonSES amazonSES;

    @Override
    public UserDto createUser(UserDto user) {

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new UserServiceException("Record already exists");
        }

        user.getAddresses().forEach(address -> {
            address.setUserDetails(user);
            address.setAddressId(utils.generatedAddressId(30));
        });
        UserEntity userEntity = modelMapper.map(user, UserEntity.class);

        String publicUserId = utils.generateUserId(30);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userEntity.setUserId(publicUserId);
        userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
        userEntity.setEmailVerificationStatus(false);

        UserEntity storedUserEntity = userRepository.save(userEntity);

        UserDto returnValue = modelMapper.map(storedUserEntity, UserDto.class);
        amazonSES.verifyEmail(returnValue);

        return returnValue;
    }

    @Override
    public UserDto getUserByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(userEntity, userDto);
        return userDto;
    }

    @Override
    public UserDto getUserById(String userId) {
        UserDto returnValue = new UserDto();
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UsernameNotFoundException(userId);
        }
        BeanUtils.copyProperties(userEntity, returnValue);
        return returnValue;
    }

    @Override
    public UserDto updateUser(String userId, UserDto user) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        UserDto returnValue = new UserDto();

        UserEntity updatedUserDetails = userRepository.save(userEntity);
        BeanUtils.copyProperties(updatedUserDetails, returnValue);

        return returnValue;
    }

    @Transactional
    @Override
    public void deleteUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) {
            throw new UserServiceException("User with ID:" + userId + " not found");
        }
        userRepository.delete(userEntity);
    }

    @Override
    public List<UserDto> getUsers(int page, int limit) {
        List<UserDto> returnValue = new ArrayList<>();
        if (page > 0) {
            page = page - 1;
        }
        Pageable pageableRequest = PageRequest.of(page, limit);

        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
        List<UserEntity> userEntities = usersPage.getContent();
        userEntities.forEach(userEntity -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(userEntity, userDto);
            returnValue.add(userDto);
        });
        return returnValue;
    }

    @Override
    public boolean verifyEmailToken(String token) {
        boolean returnValue = false;
        UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
        if (userEntity != null) {
            boolean hasTokenExpired = Utils.hasTokenExpired(token);
            if (!hasTokenExpired) {
                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(Boolean.TRUE);
                userRepository.save(userEntity);
                returnValue = true;
            }
        }

        return returnValue;
    }

    @Override
    public boolean requestPasswordReset(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            return false;
        }
        String token = Utils.generatePasswordResetToken(userEntity.getUserId());
        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(token);
        passwordResetTokenEntity.setUserDetails(userEntity);
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        return amazonSES.sendPasswordResetRequest(
                userEntity.getFirstName(),
                userEntity.getEmail(),
                token);
    }

    @Override
    public boolean resetPassword(String token, String password) {
        if (Utils.hasTokenExpired(token)) {
            return false;
        }
        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);
        if (passwordResetTokenEntity == null) {
            return false;
        }
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encodedPassword);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        boolean returnValue = savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword);
        passwordResetTokenRepository.delete(passwordResetTokenEntity);
        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) throw new UsernameNotFoundException(email);
        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(),
                userEntity.getEmailVerificationStatus(), true, true,
                true, new ArrayList<>());
    }
}
