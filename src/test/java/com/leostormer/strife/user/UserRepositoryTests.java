package com.leostormer.strife.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.leostormer.strife.AbstractRepositoryTest;

public class UserRepositoryTests extends AbstractRepositoryTest {
    
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
    void shouldExist() {
        assertTrue(userRepository.existsByUsername("testuser"));
    }

    @Test
    void shouldFindUserByUsername() {
        User user = userRepository.findOneByUsername("testuser").get();
        assertTrue(user != null && "testuser".equals(user.getUsername()));
    }

}
