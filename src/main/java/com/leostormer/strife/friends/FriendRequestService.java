package com.leostormer.strife.friends;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FriendRequestService {
    @Autowired
    private final FriendRequestRepository friendRequestRepository;

    private static final String FRIEND_REQUEST_NOT_FOUND = "Friend request not found";

    private static final String DEFAULT_UNAUTHORIZED_MESSAGE = "You are not authorized to act on this friend request";

    /**
     * Gets a friend request by its Id.
     * 
     * @param id
     * @return the friend request with the given id
     */
    public Optional<FriendRequest> getFriendRequestById(ObjectId id) {
        return friendRequestRepository.findById(id);
    }

    /**
     * Gets a friend request between user1 and user2 whether pending accepted or
     * blocked.
     * 
     * @param user1
     * @param user2
     * @return the friend request or {@link Optional#empty()} if none found
     */
    public Optional<FriendRequest> getFriendRequestByUsers(User user1, User user2) {
        return friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId());
    }

    /**
     * Returns a list of friend requests sent to or by user.
     * 
     * @param user
     * @return list of friend requests
     */
    public List<FriendRequest> getAllFriendRequests(User user) {
        return friendRequestRepository.findAllUserRequests(user.getId());
    }

    /**
     * Returns a list of friend requests sent to or by user that have been
     * accepted.
     * 
     * @param user
     * @return list of accepted friend requests
     */
    public List<FriendRequest> getAllAcceptedFriendRequests(User user) {
        return friendRequestRepository.findAllUserAcceptedRequests(user.getId());
    }

    /**
     * Returns a list of friend requests sent by or to user that have been
     * blocked.
     * 
     * @param user
     * @return list of blocked friend requests
     */
    public List<FriendRequest> getAllBlockedFriendRequests(User user) {
        return friendRequestRepository.findAllUserBlockedRequests(user.getId());
    }

    /**
     * Returns a list of friend requests sent by or to user that are still
     * pending.
     * 
     * @param user
     * @return list of pending friend requests
     */
    public List<FriendRequest> getAllPendingFriendRequests(User user) {
        return friendRequestRepository.findAllUserPendingRequests(user.getId());
    }

    /**
     * Creates a <code>FriendRequest</code> between two users.
     * 
     * @param sender   the <code>User</code> that is sending the request
     * @param receiver the <code>User</code> that is receiving the request
     * @throws UnauthorizedActionException if a friend request between these two
     *                                     users already exists
     * @return the {@link FriendRequest}
     */
    public FriendRequest sendFriendRequest(User sender, User receiver) {
        if (friendRequestRepository.findOneByUserIds(sender.getId(), receiver.getId()).isPresent()) {
            throw new UnauthorizedActionException("Friend request already exists");
        }

        FriendRequest friendRequest = FriendRequest.builder().user1(sender).user2(receiver).build();
        return friendRequestRepository.save(friendRequest);
    }

    /**
     * Accepts a friend request sent to a user.
     * 
     * @param receiver  the {@link User} who received the friend
     *                  request
     * @param requestId the id of the {@link FriendRequest}
     * @return the saved <code>FriendRequest</code>
     * @throws ResourceNotFoundException   if no such friend request
     *                                     with the given id exists
     * @throws UnauthorizedActionException if the request was not sent
     *                                     to reciever or receiver has
     *                                     been blocked
     */
    public FriendRequest acceptFriendRequest(User receiver, ObjectId requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(FRIEND_REQUEST_NOT_FOUND));

        if (!request.getUser2().getId().equals(receiver.getId())
                || request.getUser1Response().equals(FriendRequestResponse.BLOCKED))
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);

        if (request.getUser2Response().equals(FriendRequestResponse.ACCEPTED))
            return request;

        request.setUser2Response(FriendRequestResponse.ACCEPTED);
        return friendRequestRepository.save(request);
    }

    /**
     * Deletes the friend request with the given id.
     * 
     * @param user      the user requesting deletion
     * @param requestId the id of the <code>FriendRequest</code>
     * @throws ResourceNotFoundException   if no friend request exists
     *                                     with the given id
     * @throws UnauthorizedActionException if the user is not part of
     *                                     friend request or if the
     *                                     user requesting removal was
     *                                     blocked by the other user in
     *                                     the request
     */
    public void removeFriendRequest(User user, ObjectId requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(FRIEND_REQUEST_NOT_FOUND));
        if (request.isValidUser(user) && !request.hasBeenBlocked(user)) {
            friendRequestRepository.deleteById(requestId);
        } else {
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);
        }
    }

    /**
     * Creates or updates a blocked request from sender to receiver.
     * 
     * @param sender
     * @param receiver
     */
    public void blockUser(User sender, User receiver) {
        Optional<FriendRequest> result = friendRequestRepository.findOneByUserIds(sender.getId(), receiver.getId());
        if (result.isPresent() && result.get().hasSentBlockRequest(sender))
            return;

        FriendRequest friendRequest = result.orElse(FriendRequest.builder().user1(sender).user2(receiver).build());
        friendRequest.setUserResponse(sender, FriendRequestResponse.BLOCKED);
        friendRequestRepository.save(friendRequest);
    }

    /**
     * Removes the blocked request sent from sender to receiver.
     * 
     * @param sender
     * @param receiver
     * @throws UnauthorizedActionException if sender has not blocked
     *                                     receiver
     */
    public void unblockUser(User sender, User receiver) {
        Optional<FriendRequest> friendRequestOptional = friendRequestRepository.findOneByUserIds(sender.getId(),
                receiver.getId());

        if (friendRequestOptional.isEmpty() || !friendRequestOptional.get().hasSentBlockRequest(sender))
            throw new UnauthorizedActionException("Cannot unblock user that has not been blocked");

        FriendRequest friendRequest = friendRequestOptional.get();
        if (friendRequest.hasBeenBlocked(sender)) {
            friendRequest.setUserResponse(sender, FriendRequestResponse.PENDING);
            friendRequestRepository.save(friendRequest);
        } else {
            friendRequestRepository.delete(friendRequest);
        }
    }
}
