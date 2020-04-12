package com.prokopchuk.ws;

import com.prokopchuk.ws.io.entity.AuthorityEntity;
import com.prokopchuk.ws.io.entity.RoleEntity;
import com.prokopchuk.ws.io.entity.UserEntity;
import com.prokopchuk.ws.io.repositories.AuthorityRepository;
import com.prokopchuk.ws.io.repositories.RoleRepository;
import com.prokopchuk.ws.io.repositories.UserRepository;
import com.prokopchuk.ws.shared.Roles;
import com.prokopchuk.ws.shared.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;

@Component
public class InitialUserSetup {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Utils utils;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @EventListener
    @Transactional
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        AuthorityEntity readAuthority = createAuthority("READ_AUTHORITY");
        AuthorityEntity writeAuthority = createAuthority("WRITE_AUTHORITY");
        AuthorityEntity deleteAuthority = createAuthority("DELETE_AUTHORITY");

        RoleEntity roleUser = createRole(Roles.ROLE_USER.name(), Arrays.asList(readAuthority, writeAuthority));
        RoleEntity roleAdmin = createRole(Roles.ROLE_ADMIN.name(), Arrays.asList(readAuthority, writeAuthority, deleteAuthority));

        UserEntity adminUser = new UserEntity();
        adminUser.setFirstName("Nikolay");
        adminUser.setLastName("Nikolayeev");
        adminUser.setEmail("test@test.com");
        adminUser.setEmailVerificationStatus(true);
        adminUser.setUserId(utils.generateUserId(30));
        adminUser.setEncryptedPassword(passwordEncoder.encode("12345678"));
        adminUser.setRoles(Arrays.asList(roleAdmin));

        UserEntity user1 = new UserEntity();
        user1.setFirstName("Nikolay");
        user1.setLastName("Prokopchuk");
        user1.setEmail("nikolay@test.com");
        user1.setEmailVerificationStatus(true);
        user1.setUserId(utils.generateUserId(30));
        user1.setEncryptedPassword(passwordEncoder.encode("123"));
        user1.setRoles(Arrays.asList(roleUser));

        UserEntity user2 = new UserEntity();
        user2.setFirstName("Igor");
        user2.setLastName("Prokopchuk");
        user2.setEmail("igor@test.com");
        user2.setEmailVerificationStatus(true);
        user2.setUserId(utils.generateUserId(30));
        user2.setEncryptedPassword(passwordEncoder.encode("123"));
        user2.setRoles(Arrays.asList(roleUser));

        userRepository.save(adminUser);
        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Transactional
    private AuthorityEntity createAuthority(String name) {
        AuthorityEntity authority = authorityRepository.findByName(name);
        if (authority == null) {
            authority = new AuthorityEntity(name);
            authorityRepository.save(authority);
        }
        return authority;
    }

    @Transactional
    private RoleEntity createRole(String name, Collection<AuthorityEntity> authorities) {
        RoleEntity role = roleRepository.findByName(name);
        if (role == null) {
            role = new RoleEntity(name);
            role.setAuthorities(authorities);
            roleRepository.save(role);
        }
        return role;
    }
}
