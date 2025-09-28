package com.leostormer.strife.user.friends;

import java.util.List;

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
     * Returns a list of friend requests sent to or by user.
     * 
     * @param user
     * @return list of friend requests
     */
    public List<FriendRequest> getAllFriendRequests(User user) {
        return friendRequestRepository.findAllUserRequests(user.getId());
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

        FriendRequest friendRequest = FriendRequest.builder().sender(sender).receiver(receiver).build();
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
     *                                     to receiver, or receiver has
     *                                     been blocked
     */
    public FriendRequest acceptFriendRequest(User receiver, ObjectId requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(FRIEND_REQUEST_NOT_FOUND));

        if (!request.getReceiver().getId().equals(receiver.getId())
                || request.getOtherUser(receiver).hasBlocked(receiver))
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);

        if (request.isAccepted())
            return request;

        request.setAccepted(true);
        return friendRequestRepository.save(request);
    }

    /**
     * Deletes the friend request with the given id.
     * 
     * @param user      the user requesting deletion
     * @param requestId the id of the <code>FriendRequest</code>
     * @return the {@link User} that was the other party for convenience (e.g. to
     *         update friends list)
     * @throws ResourceNotFoundException   if no friend request exists
     *                                     with the given id
     * @throws UnauthorizedActionException if the user is not part of
     *                                     friend request or if the
     *                                     user requesting removal was
     *                                     blocked by the other user in
     *                                     the request
     */
    public User removeFriendRequest(User user, ObjectId requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(FRIEND_REQUEST_NOT_FOUND));

        if (!request.isValidUser(user))
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);

        friendRequestRepository.deleteById(requestId);
        return request.getOtherUser(user);
    }
}
