package com.leostormer.strife.user.friends;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractRepositoryTest;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

public class FriendRequestRepositoryTests extends AbstractRepositoryTest {

    @Autowired
    FriendRequestRepository friendRequestRepository;
    @Autowired
    UserRepository userRepository;

    @BeforeAll
    static void setUpUsers(@Autowired UserRepository userRepository) {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password123");
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password1234");

        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password12345");

        User user4 = new User();
        user4.setUsername("user4");
        user4.setPassword("password123456");

        User user5 = new User();
        user5.setUsername("user5");
        user5.setPassword("password1234567");

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
    }

    @BeforeEach
    void setup() {
        User user1 = userRepository.findOneByUsername("user1").get();
        User user2 = userRepository.findOneByUsername("user2").get();
        User user3 = userRepository.findOneByUsername("user3").get();
        User user4 = userRepository.findOneByUsername("user4").get();

        friendRequestRepository.saveAll(List.of(
                FriendRequest.builder()
                        .sender(user1)
                        .receiver(user2)
                        .build(),
                FriendRequest.builder()
                        .sender(user1)
                        .receiver(user3)
                        .accepted(true)
                        .build(),
                FriendRequest.builder()
                        .sender(user4)
                        .receiver(user1)
                        .build()));
    }

    @AfterEach
    void cleanUp() {
        friendRequestRepository.deleteAll();
    }

    @AfterAll
    static void clearUsers(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindAllUserRequests() {
        User user1 = userRepository.findOneByUsername("user1").get();

        List<FriendRequest> requests = friendRequestRepository.findAllUserRequests(user1.getId());
        assertTrue(requests.size() == 3);
    }

    @Test
    void shouldFindRequestByUserIdsWithoutCaringAboutOrder() {
        User user1 = userRepository.findOneByUsername("user1").get();
        User user2 = userRepository.findOneByUsername("user2").get();

        Optional<FriendRequest> request = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId());
        Optional<FriendRequest> request2 = friendRequestRepository.findOneByUserIds(user2.getId(), user1.getId());
        assertTrue(request.isPresent());
        assertTrue(request2.isPresent());
        assertTrue(request.get().getId().equals(request2.get().getId()));
    }

    @Test
    void shouldFindAllUserAcceptedRequests() {
        User user1 = userRepository.findOneByUsername("user1").get();
        User user2 = userRepository.findOneByUsername("user2").get();
        User user3 = userRepository.findOneByUsername("user3").get();
        User user4 = userRepository.findOneByUsername("user4").get();
        User user5 = userRepository.findOneByUsername("user5").get();

        List<FriendRequest> requests = friendRequestRepository.findAllUserAcceptedRequests(user1.getId());
        assertTrue(requests.size() == 1);

        requests = friendRequestRepository.findAllUserAcceptedRequests(user2.getId());
        assertTrue(requests.size() == 0);

        requests = friendRequestRepository.findAllUserAcceptedRequests(user3.getId());
        assertTrue(requests.size() == 1);

        requests = friendRequestRepository.findAllUserAcceptedRequests(user4.getId());
        assertTrue(requests.size() == 0);

        requests = friendRequestRepository.findAllUserAcceptedRequests(user5.getId());
        assertTrue(requests.size() == 0);
    }

    @Test
    void shouldFindAllUserPendingRequests() {
        User user1 = userRepository.findOneByUsername("user1").get();
        User user2 = userRepository.findOneByUsername("user2").get();
        User user3 = userRepository.findOneByUsername("user3").get();
        User user4 = userRepository.findOneByUsername("user4").get();
        User user5 = userRepository.findOneByUsername("user5").get();

        List<FriendRequest> requests = friendRequestRepository.findAllUserPendingRequests(user1.getId());
        assertTrue(requests.size() == 2);

        requests = friendRequestRepository.findAllUserPendingRequests(user2.getId());
        assertTrue(requests.size() == 1);

        requests = friendRequestRepository.findAllUserPendingRequests(user3.getId());
        assertTrue(requests.size() == 0);

        requests = friendRequestRepository.findAllUserPendingRequests(user4.getId());
        assertTrue(requests.size() == 1);

        requests = friendRequestRepository.findAllUserPendingRequests(user5.getId());
        assertTrue(requests.size() == 0);
    }

    @Test
    void shouldExistByUserIdsWithoutCaringAboutOrder() {
        User user1 = userRepository.findOneByUsername("user1").get();
        User user3 = userRepository.findOneByUsername("user3").get();

        assertTrue(friendRequestRepository.existsByUserIds(user1.getId(), user3.getId()));
        assertTrue(friendRequestRepository.existsByUserIds(user3.getId(), user1.getId()));
    }
}
