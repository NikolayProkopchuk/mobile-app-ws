package com.prokopchuk.ws.service.impl;

import com.prokopchuk.ws.exceptions.UserServiceException;
import com.prokopchuk.ws.io.entity.AddressEntity;
import com.prokopchuk.ws.io.entity.UserEntity;
import com.prokopchuk.ws.io.repositories.PasswordResetTokenRepository;
import com.prokopchuk.ws.io.repositories.UserRepository;
import com.prokopchuk.ws.service.AmazonSES;
import com.prokopchuk.ws.shared.Utils;
import com.prokopchuk.ws.shared.dto.AddressDto;
import com.prokopchuk.ws.shared.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Utils utils;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private AmazonSES amazonSES;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private String userId = "hhty57ehfy";
    private String email = "test@test.com";
    private String encryptedPassword = "74hgh8474jf";
    private String emailVerificationToken = "sjkfkglhierjwmvlhj";
    private UserEntity userEntity;
    private String password = "12345678";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstName("Nikolay");
        userEntity.setLastName("Nikolayev");
        userEntity.setUserId(userId);
        userEntity.setEncryptedPassword(encryptedPassword);
        userEntity.setEmail(email);
        userEntity.setEmailVerificationToken(emailVerificationToken);
        userEntity.setAddresses(getListOfAddressEntities());
    }

    @Test
    final void testGetUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

        UserDto userDto = userService.getUserByEmail("test@test.com");
        assertNotNull(userDto);
        assertEquals("Nikolay", userDto.getFirstName());
    }

    @Test
    final void testGetUser_UserNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("test@test.com")
        );
    }

    @Test
    final void testCreateUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(utils.generatedAddressId(anyInt())).thenReturn("hhty57ehfy");
        when(utils.generateUserId(anyInt())).thenReturn(userId);
        when(bCryptPasswordEncoder.encode(password)).thenReturn(encryptedPassword);
        when(utils.generateEmailVerificationToken(anyString())).thenReturn(emailVerificationToken);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        doNothing().when(amazonSES).verifyEmail(any());

        UserDto argumentUserDto = getUserDto();

        UserDto storedUserDto = userService.createUser(argumentUserDto);
        assertNotNull(storedUserDto);
        assertEquals(argumentUserDto.getFirstName(), storedUserDto.getFirstName());
        assertEquals(argumentUserDto.getLastName(), storedUserDto.getLastName());
        assertEquals(argumentUserDto.getEmail(), storedUserDto.getEmail());
        assertNotNull(storedUserDto.getUserId());
        assertEquals(argumentUserDto.getAddresses().size(), storedUserDto.getAddresses().size());
        verify(utils, times(2)).generatedAddressId(30);
        verify(bCryptPasswordEncoder, times(1)).encode(password);
        verify(userRepository,times(1)).save(any(UserEntity.class));
    }

    private UserDto getUserDto() {
        List<AddressDto> addresses = getListOfAddressDtos();

        UserDto argumentUserDto = new UserDto();
        argumentUserDto.setFirstName("Nikolay");
        argumentUserDto.setLastName("Nikolayev");
        argumentUserDto.setPassword(password);
        argumentUserDto.setEmail(email);
        argumentUserDto.setAddresses(addresses);
        return argumentUserDto;
    }

    @Test
    final void testCreateUser_UserServiceException() {
        when(userRepository.findByEmail(anyString())).thenReturn(userEntity);
        assertThrows(UserServiceException.class,
                () -> userService.createUser(getUserDto())
        );
    }

    private List<AddressDto> getListOfAddressDtos() {
        AddressDto shippingAddressDto = new AddressDto();
        shippingAddressDto.setType("shipping");
        shippingAddressDto.setCity("Kharkov");
        shippingAddressDto.setCountry("Ukraine");
        shippingAddressDto.setPostalCode("ABC123");
        shippingAddressDto.setStreetName("123 Street name");

        AddressDto billingAddressDto = new AddressDto();
        billingAddressDto.setType("billing");
        billingAddressDto.setCity("Kharkov");
        billingAddressDto.setCountry("Ukraine");
        billingAddressDto.setPostalCode("ABC123");
        billingAddressDto.setStreetName("123 Street name");
        return List.of(shippingAddressDto, billingAddressDto);
    }

    private List<AddressEntity> getListOfAddressEntities() {
        AddressEntity shippingAddressEntity = new AddressEntity();
        shippingAddressEntity.setType("shipping");
        shippingAddressEntity.setCity("Kharkov");
        shippingAddressEntity.setCountry("Ukraine");
        shippingAddressEntity.setPostalCode("ABC123");
        shippingAddressEntity.setStreetName("123 Street name");

        AddressEntity billingAddressEntity = new AddressEntity();
        billingAddressEntity.setType("billing");
        billingAddressEntity.setCity("Kharkov");
        billingAddressEntity.setCountry("Ukraine");
        billingAddressEntity.setPostalCode("ABC123");
        billingAddressEntity.setStreetName("123 Street name");
        return List.of(shippingAddressEntity, billingAddressEntity);
    }
}
