package com.prokopchuk.ws.service.impl;

import com.prokopchuk.ws.io.entity.AddressEntity;
import com.prokopchuk.ws.io.entity.UserEntity;
import com.prokopchuk.ws.io.repositories.AddressRepository;
import com.prokopchuk.ws.io.repositories.UserRepository;
import com.prokopchuk.ws.service.AddressService;
import com.prokopchuk.ws.shared.dto.AddressDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<AddressDto> getAddresses(String userId) {
        List<AddressDto> returnValue = new ArrayList<>();
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) return returnValue;

        List<AddressEntity> addresses = addressRepository.findAllByUserDetails(userEntity);
        Type listType = new TypeToken<List<AddressDto>>(){}.getType();

        return modelMapper.map(addresses, listType);
    }

    @Override
    public AddressDto getAddress(String addressId) {
        AddressDto returnValue = null;
        AddressEntity addressEntity = addressRepository.findByAddressId(addressId);
        if (addressEntity != null) {
            returnValue = modelMapper.map(addressEntity, AddressDto.class);
        }
        return returnValue;
    }
}
