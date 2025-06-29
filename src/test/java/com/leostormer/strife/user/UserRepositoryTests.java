package com.leostormer.strife.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

@DataMongoTest
public class UserRepositoryTests {
    
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        userRepository.save(user);
    }
    
    @AfterEach
    void clearDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldNotBeEmpty() {
        assertFalse(userRepository.findAll().isEmpty());
    }


    @Test
    void shouldSaveUser() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("newpassword");
        userRepository.save(user);
        
        assertTrue(userRepository.existsByUsername("newuser"));
    }

    @Test
    void shouldOnlyHaveOneUser() {
        long count = userRepository.count();
        assertTrue(count == 1);
    }

    @Test
    void shouldFindUserByUsername() {
        User user = userRepository.findOneByUsername("testuser").get();
        assertTrue(user != null && "testuser".equals(user.getUsername()));
    }

}
