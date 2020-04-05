package com.prokopchuk.ws.share;

import com.prokopchuk.ws.shared.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UtilsTest {

    @Autowired
    private Utils utils;

    @BeforeEach
    void setUp() throws Exception {

    }

    @Test
    final void testGenerateUserId() {
        String userId1 = utils.generateUserId(30);
        String userId2 = utils.generateUserId(30);

        assertNotNull(userId1);
        assertEquals(30, userId1.length());
        assertNotEquals(userId1, userId2);
    }

    @Test
    final void testHasNotTokenExpired() {
        String token = utils.generateEmailVerificationToken("treru1eryt");
        assertNotNull(token);

        boolean hasTokenExpired = Utils.hasTokenExpired(token);
        assertFalse(hasTokenExpired);
    }

    @Test
    @Disabled
    final void testHasTokenExpired() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzTDh3cHJ3VHVJcTk4Sk1nVmtraWJGZ09ZUGJjVTAiLCJleHAiOjE1ODYwMDg3NjN9.6LrCxjFenWNBhgqYF-PPWInHWDlqUefUTFXpi2KrrueXDGy0RPvkUpLGzjD2ketVdekN0R9LKheFMiIk4Po3fA";
        assertNotNull(token);

        boolean hasTokenExpired = Utils.hasTokenExpired(token);
        assertTrue(hasTokenExpired);
    }
}
