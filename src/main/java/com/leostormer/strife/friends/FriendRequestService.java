package com.leostormer.strife.friends;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FriendRequestService {
    @Autowired
    private final FriendRequestRepository friendRequestRepository;

    public Optional<FriendRequest> getFriendRequestById(ObjectId id) {
        return friendRequestRepository.findById(id);
    }

    /**
     * Returns a list of friend requests sent to or by user.
     * 
     * @param user
     * @return all friend requests
     */
    public List<FriendRequest> getAllFriendRequests(User user) {
        return friendRequestRepository.findAllUserRequests(user.getId());
    }

    /**
     * Returns a list of friend requests sent to or by user that have been
     * accepted.
     * 
     * @param user
     * @return all accepted friend requests
     */
    public List<FriendRequest> getAllAcceptedFriendRequests(User user) {
        return friendRequestRepository.findAllUserAcceptedRequests(user.getId());
    }

    /**
     * Returns a list of friend requests sent by or to user that have been
     * blocked.
     * 
     * @param user
     * @return all blocked friend requests
     */
    public List<FriendRequest> getAllBlockedFriendRequests(User user) {
        return friendRequestRepository.findAllUserBlockedRequests(user.getId());
    }

    /**
     * Returns a list of friend requests sent by or to user that are still
     * pending.
     * 
     * @param user
     * @return all pending friend requests
     */
    public List<FriendRequest> getAllPendingFriendRequests(User user) {
        return friendRequestRepository.findAllUserPendingRequests(user.getId());
    }

    /**
     * Creates a friend request between two users.
     * 
     * @param sender   the <code>User</code> that is sending the request
     * @param receiver the <code>User</code> that is receiving the request
     * @throws DuplicateFriendRequestException if a friend request between these two
     *                                         users already exists
     */
    public FriendRequest sendFriendRequest(User sender, User receiver) {
        if (friendRequestRepository.findOneByUserIds(sender.getId(), receiver.getId()).isPresent()) {
            throw new DuplicateFriendRequestException();
        }

        FriendRequest friendRequest = FriendRequest.builder().user1(sender).user2(receiver).build();
        return friendRequestRepository.save(friendRequest);
    }

    /**
     * Accepts a friend request sent to a user.
     * 
     * @param receiver  the <code>User</code> who received the friend
     *                  request
     * @param requestId the id of the <code>FriendRequest</code>
     * @return the saved friend request object
     * @throws FriendRequestNotFoundException           if no such friend request
     *                                                  with id == requestId exists
     * @throws UnauthorizedFriendRequestActionException if the request was not sent
     *                                                  to reciever or receiver has
     *                                                  been blocked
     */
    public FriendRequest acceptFriendRequest(User receiver, ObjectId requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(FriendRequestNotFoundException::new);
                
        if (!request.getUser2().getId().equals(receiver.getId()) || request.getUser1Response().equals(FriendRequestResponse.BLOCKED))
            throw new UnauthorizedFriendRequestActionException();

        if (request.getUser2Response().equals(FriendRequestResponse.ACCEPTED))
            return request;

        request.setUser2Response(FriendRequestResponse.ACCEPTED);
        return friendRequestRepository.save(request);
    }

    public void removeFriendRequest(User user, ObjectId requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(FriendRequestNotFoundException::new);
        if (request.isValidUser(user) && !request.hasBeenBlocked(user)) {
            friendRequestRepository.deleteById(requestId);
        } else {
            throw new UnauthorizedFriendRequestActionException();
        }
    }

    public void blockUser(User sender, User receiver) {
        Optional<FriendRequest> result = friendRequestRepository.findOneByUserIds(sender.getId(), receiver.getId());
        if (result.isPresent() && result.get().hasSentBlockRequest(sender))
            return;

        FriendRequest friendRequest = result.orElse(FriendRequest.builder().user1(sender).user2(receiver).build());
        friendRequest.setUserResponse(sender, FriendRequestResponse.BLOCKED);
        friendRequestRepository.save(friendRequest);
    }

    public void unblockUser(User sender, User receiver) {
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(sender.getId(), receiver.getId())
                .orElseThrow(() -> new FriendRequestNotFoundException("Blocked Friend Request Not Found"));

        if (!friendRequest.hasSentBlockRequest(sender))
            throw new UnauthorizedFriendRequestActionException("Cannot unblock user that has not been blocked");

        if (friendRequest.hasBeenBlocked(sender)) {
            friendRequest.setUserResponse(sender, FriendRequestResponse.PENDING);
            friendRequestRepository.save(friendRequest);
        } else {
            friendRequestRepository.delete(friendRequest);
        }
    }
}
