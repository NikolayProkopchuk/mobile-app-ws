package com.prokopchuk.ws.ui.controller;

import com.prokopchuk.ws.exceptions.UserServiceException;
import com.prokopchuk.ws.service.AddressService;
import com.prokopchuk.ws.service.UserService;
import com.prokopchuk.ws.shared.dto.AddressDto;
import com.prokopchuk.ws.shared.dto.UserDto;
import com.prokopchuk.ws.ui.model.request.PasswordResetModel;
import com.prokopchuk.ws.ui.model.request.PasswordResetRequestModel;
import com.prokopchuk.ws.ui.model.request.UserDetailsRequestModel;
import com.prokopchuk.ws.ui.model.response.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public EntityModel<UserRest> getUser(@PathVariable String id) {
        UserRest returnValue = new UserRest();

        UserDto userDto = userService.getUserById(id);
        if (userDto != null) {
            returnValue = modelMapper.map(userDto, UserRest.class);
        }
        Link userLink = linkTo(methodOn(UserController.class).getUser(id))
                .withSelfRel();
        Link addressesLink = linkTo(methodOn(UserController.class).getUserAddresses(id))
                .withRel("addresses");
        returnValue.add(userLink)
                .add(addressesLink);
        return new EntityModel<>(returnValue);
    }

    @PostMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) {
        if (userDetails.getFirstName().isEmpty()) {
            throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
        }
        UserDto userDto = modelMapper.map(userDetails, UserDto.class);

        UserDto createdUser = userService.createUser(userDto);
        return modelMapper.map(createdUser, UserRest.class);
    }

    @PutMapping(path = "/{id}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
        UserDto userDto = modelMapper.map(userDetails, UserDto.class);
        UserDto updatedUser = userService.updateUser(id, userDto);

        return modelMapper.map(updatedUser, UserRest.class);
    }

    @DeleteMapping(path = "/{id}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OperationStatusModel deleteUser(@PathVariable String id) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        userService.deleteUser(id);
        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return returnValue;
    }

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public CollectionModel<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "limit", defaultValue = "25") int limit) {
        List<UserRest> usersListRestModel = new ArrayList<>();
        List<UserDto> users = userService.getUsers(page, limit);

        if (users != null && !users.isEmpty()) {
            Type listType = new TypeToken<List<UserRest>>(){}.getType();
            usersListRestModel = modelMapper.map(users, listType);
            usersListRestModel.forEach(userRest -> {
                Link userLink = linkTo(methodOn(UserController.class).getUser(userRest.getUserId()))
                        .withRel("user");
                userRest.add(userLink);

            });
        }
        return new CollectionModel<>(usersListRestModel);
    }

    @GetMapping(path = "/{userId}/addresses", produces = {MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public CollectionModel<AddressRest> getUserAddresses(@PathVariable String userId) {
        List<AddressDto> addressesDTO = addressService.getAddresses(userId);
        List<AddressRest> addressesListRestModel = new ArrayList<>();
        if (addressesDTO != null && !addressesDTO.isEmpty()) {
            Type listType = new TypeToken<List<AddressRest>>(){}.getType();
            addressesListRestModel = modelMapper.map(addressesDTO, listType);
        }
        Link userLink = linkTo(methodOn(UserController.class).getUser(userId))
                .withRel("user");
        addressesListRestModel.forEach(addressRest -> {
            Link addressLink = linkTo(methodOn(UserController.class).getAddress(userId, addressRest.getAddressId()))
                    .withSelfRel();
            addressRest.add(userLink)
                    .add(addressLink);
        });
        return new CollectionModel<>(addressesListRestModel);
    }

    @GetMapping(path = "/{userId}/addresses/{addressId}", produces = {MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public EntityModel<AddressRest> getAddress(@PathVariable String userId, @PathVariable String addressId) {
        AddressDto addressDTO = addressService.getAddress(addressId);
        Link addressLink = linkTo(methodOn(UserController.class).getAddress(userId, addressId))
                .withSelfRel();
        Link userLink = linkTo(methodOn(UserController.class).getUser(userId))
                .withRel("user");
        AddressRest addressRestModel = modelMapper.map(addressDTO, AddressRest.class);
        addressRestModel.add(addressLink)
                .add(userLink);
        return new EntityModel<>(addressRestModel);
    }

    @GetMapping(path = "/email-verification", produces = MediaType.APPLICATION_JSON_VALUE)
    public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
        boolean isVerified = userService.verifyEmailToken(token);
        if (isVerified) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        } else {
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }
        return returnValue;
    }

    @PostMapping(path = "/password-reset-request",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
        OperationStatusModel returnValue = new OperationStatusModel();
        boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
        returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        if (operationResult) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }

        return returnValue;
    }

    @PostMapping(path = "/password-reset",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
        OperationStatusModel returnValue = new OperationStatusModel();
        boolean operationResult = userService.resetPassword(
                passwordResetModel.getToken(),
                passwordResetModel.getPassword());
        returnValue.setOperationName(RequestOperationStatus.PASSWORD_REST_NAME.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        if (operationResult) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }
        return returnValue;
    }
}
