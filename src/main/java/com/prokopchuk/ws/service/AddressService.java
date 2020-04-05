package com.prokopchuk.ws.service;

import com.prokopchuk.ws.shared.dto.AddressDto;

import java.util.List;

public interface AddressService {
    List<AddressDto> getAddresses(String userId);

    AddressDto getAddress(String addressId);
}
