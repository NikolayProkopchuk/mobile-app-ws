package com.prokopchuk.ws.ui.controller;

import com.prokopchuk.ws.service.impl.UserServiceImpl;
import com.prokopchuk.ws.shared.dto.AddressDto;
import com.prokopchuk.ws.shared.dto.UserDto;
import com.prokopchuk.ws.ui.model.response.UserRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserServiceImpl userService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private UserDto userDto;
    private final String USER_ID = "qklriepwi";


    @BeforeEach
    private void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        userDto = new UserDto();
        userDto.setFirstName("Nikolay");
        userDto.setLastName("Nikolayev");
        userDto.setEmailVerificationStatus(false);
        userDto.setEmailVerificationToken(null);
        userDto.setUserId(USER_ID);
        userDto.setPassword("12345");
        userDto.setEmail("test@test.com");
        userDto.setAddresses(getListOfAddressDtos());
    }

    @Test
    public final void testGetUser() {
        when(userService.getUserById(anyString())).thenReturn(userDto);
        EntityModel<UserRest> entityModel = userController.getUser(USER_ID);
        UserRest userRest = entityModel.getContent();
        assertNotNull(userRest);
        assertEquals(USER_ID, userRest.getUserId());
        assertEquals(userDto.getFirstName(), userRest.getFirstName());
        assertEquals(userDto.getLastName(), userRest.getLastName());
        assertEquals(userDto.getAddresses().size(), userRest.getAddresses().size());
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

}
