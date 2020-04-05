package com.prokopchuk.ws;

import com.prokopchuk.ws.io.entity.AddressEntity;
import com.prokopchuk.ws.io.entity.UserEntity;
import com.prokopchuk.ws.io.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private static boolean isRecordsCreated = false;

    @BeforeEach
    void setUp() {
        if (!UserRepositoryTest.isRecordsCreated) {
            createRecords();
        }
    }

    @Test
    final void testGetVerifiedUsers() {
        Pageable pageableRequest = PageRequest.of(0, 1);
        Page<UserEntity> pages = userRepository.findAllUsersWithConfirmedEmailAddress(pageableRequest);
        assertNotNull(pages);

        List<UserEntity> userEntities = pages.getContent();
        assertNotNull(userEntities);
        assertEquals(1, userEntities.size());
    }

    @Test
    final void testFindUserByFirstName() {
        String fistName = "Nikolay";
        List<UserEntity> userEntities = userRepository.findUserByFirstName(fistName);
        assertNotNull(userEntities);
        assertEquals(1, userEntities.size());

        assertEquals(fistName, userEntities.get(0).getFirstName());
    }

    @Test
    final void testFindUserByLastName() {
        String lastName = "Nikolayev";
        List<UserEntity> userEntities = userRepository.findUserByLastName(lastName);
        assertNotNull(userEntities);
        assertEquals(1, userEntities.size());

        assertEquals(lastName, userEntities.get(0).getLastName());
    }

    @Test
    final void testFindUserByKeyword() {
        String keyword = "Nik";
        List<UserEntity> userEntities = userRepository.findUserByKeyword(keyword);
        assertNotNull(userEntities);
        assertEquals(1, userEntities.size());

        assertTrue(userEntities.get(0).getFirstName().contains(keyword) ||
                userEntities.get(0).getLastName().contains(keyword));
    }

    @Test
    final void testFindUserFirstNameAndLastByKeyword() {
        String keyword = "Nik";
        List<Object[]> userFirstAndLastNames = userRepository.findUserFirstNameAndLastByKeyword(keyword);
        Object[] userFirstAndLastName = userFirstAndLastNames.get(0);

        assertNotNull(userFirstAndLastName);
        assertEquals(1, userFirstAndLastNames.size());
        assertEquals(2, userFirstAndLastName.length);

        String firstName = (String) userFirstAndLastName[0];
        String lastName = (String) userFirstAndLastName[1];

        assertNotNull(firstName);
        assertNotNull(lastName);

        assertTrue(firstName.contains(keyword) ||
                lastName.contains(keyword));
    }

    @Test
    final void testUpdateUserEmailVerificationStatus() {
        boolean newEmailVerificationStatus = false;
        String userId = "1fghij";
        userRepository.updateUserEmailVerificationStatus(newEmailVerificationStatus, userId);

        UserEntity storedUserEntity = userRepository.findByUserId(userId);

        assertNotNull(storedUserEntity);
        assertEquals(newEmailVerificationStatus, storedUserEntity.getEmailVerificationStatus());
    }

    @Test
    final void testFindUserByUserId() {
        String userId = "1fghij";
        UserEntity userEntity = userRepository.findUserByUserId(userId);

        assertNotNull(userEntity);
        assertEquals(userId, userEntity.getUserId());
    }

    @Test
    final void testFindUserEntityFullNameByUserId() {
        String userId = "1fghij";
        List<Object[]> userFullNames = userRepository.findUserEntityFullNameByUserId(userId);
        Object[] userFullName = userFullNames.get(0);

        assertNotNull(userFullName);
        assertEquals(1, userFullNames.size());
        assertEquals(2, userFullName.length);

        String firstName = (String) userFullName[0];
        String lastName = (String) userFullName[1];

        assertNotNull(firstName);
        assertNotNull(lastName);
    }

    private void createRecords() {
        UserEntity userEntity1 = new UserEntity();
        userEntity1.setFirstName("Nikolay");
        userEntity1.setLastName("Nikolayev");
        userEntity1.setUserId("1asdfg");
        userEntity1.setEncryptedPassword("xxx");
        userEntity1.setEmail("test1@test.com");
        userEntity1.setEmailVerificationStatus(true);

        AddressEntity addressEntity1 = new AddressEntity();
        addressEntity1.setType("shipping");
        addressEntity1.setAddressId("asdfgjhjk;l");
        addressEntity1.setCity("Kharkov");
        addressEntity1.setCountry("Ukraine");
        addressEntity1.setPostalCode("ABCCDA");
        addressEntity1.setStreetName("123 Street Name");

        List<AddressEntity> addresses1 = List.of(addressEntity1);
        userEntity1.setAddresses(addresses1);

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setFirstName("Max");
        userEntity2.setLastName("Maximov");
        userEntity2.setUserId("1fghij");
        userEntity2.setEncryptedPassword("xxx");
        userEntity2.setEmail("test2@test.com");
        userEntity2.setEmailVerificationStatus(true);

        AddressEntity addressEntity2 = new AddressEntity();
        addressEntity2.setType("shipping");
        addressEntity2.setAddressId("jghfggjk");
        addressEntity2.setCity("Kharkov");
        addressEntity2.setCountry("Ukraine");
        addressEntity2.setPostalCode("ABCCDA");
        addressEntity2.setStreetName("456 Street Name");

        List<AddressEntity> addresses2 = List.of(addressEntity2);
        userEntity2.setAddresses(addresses2);

        userRepository.save(userEntity1);
        userRepository.save(userEntity2);

        UserRepositoryTest.isRecordsCreated = true;
    }

    @Test
    final void testUpdateUserEntityEmailVerificationStatus() {
        boolean newEmailVerificationStatus = true;
        String userId = "1fghij";
        userRepository.updateUserEntityEmailVerificationStatus(newEmailVerificationStatus, userId);

        UserEntity storedUserEntity = userRepository.findByUserId(userId);

        assertNotNull(storedUserEntity);
        assertEquals(newEmailVerificationStatus, storedUserEntity.getEmailVerificationStatus());
    }
}
