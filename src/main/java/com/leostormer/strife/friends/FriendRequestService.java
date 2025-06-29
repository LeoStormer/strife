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
     * Returns a list of friend requests sent by or to user.
     * 
     * @param user
     * @return all friend requests
     */
    public List<FriendRequest> getAllFriendRequests(User user) {
        return friendRequestRepository.findBySenderIdOrReceiverId(user.getId(), user.getId());
    }

    /**
     * Returns a list of friend requests sent by or to user that have been
     * accepted.
     * 
     * @param user
     * @return all accepted friend requests
     */
    public List<FriendRequest> getAllAcceptedFriendRequests(User user) {
        return friendRequestRepository.findBySenderIdOrReceiverIdAndStatus(user.getId(), user.getId(),
                FriendStatus.ACCEPTED);
    }

    /**
     * Returns a list of friend requests sent by or to user that have been
     * blocked.
     * 
     * @param user
     * @return all blocked friend requests
     */
    public List<FriendRequest> getAllBlockedFriendRequests(User user) {
        return friendRequestRepository.findBySenderIdOrReceiverIdAndStatus(user.getId(), user.getId(),
                FriendStatus.BLOCKED);
    }

    /**
     * Returns a list of friend requests sent by or to user that are still
     * pending.
     * 
     * @param user
     * @return all pending friend requests
     */
    public List<FriendRequest> getAllPendingFriendRequests(User user) {
        return friendRequestRepository.findBySenderIdOrReceiverIdAndStatus(user.getId(), user.getId(),
                FriendStatus.PENDING);
    }

    /**
     * Creates a friend request between two users.
     * 
     * @param sender the <code>User</code> that is sending the request
     * @param receiver the <code>User</code> that is receiving the request
     * @throws DuplicateFriendRequestException if a friend request between these two users already exists
     */
    public FriendRequest sendFriendRequest(User sender, User receiver) {
        if (friendRequestRepository.existsBySenderIdAndReceiverId(sender.getId(), receiver.getId())
                || friendRequestRepository.existsBySenderIdAndReceiverId(receiver.getId(), sender.getId())) {
            throw new DuplicateFriendRequestException();
        }

        FriendRequest friendRequest = FriendRequest.builder().sender(sender).receiver(receiver).build();
        return friendRequestRepository.save(friendRequest);
    }

    /**
     * Accepts a friend request sent to a user.
     * 
     * @param requestId the id of the <code>FriendRequest</code>
     * @param receiverId the id of the <code>User</code> who received the friend request
     * @return the saved friend request object 
     * @throws FriendRequestNotFoundException if no such friend request with id == requestId exists
     * @throws UnauthorizedFriendRequestActionException if the friend request's status is blocked or if it was not sent to recieverId
     */
    public FriendRequest acceptFriendRequest(User receiver, ObjectId requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(FriendRequestNotFoundException::new);
        if (!request.getReceiver().equals(receiver) || request.getStatus().equals(FriendStatus.BLOCKED))
            throw new UnauthorizedFriendRequestActionException();

        if (request.getStatus().equals(FriendStatus.ACCEPTED))
            return request;

        request.setStatus(FriendStatus.ACCEPTED);
        return friendRequestRepository.save(request);
    }

    public void removeFriendRequest(User user, ObjectId requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(FriendRequestNotFoundException::new);
        if (request.getSender().equals(user) || request.getReceiver().equals(user)) {
            friendRequestRepository.deleteById(requestId);
        } else {
            throw new UnauthorizedFriendRequestActionException();
        }
    }

    public void blockUser(User sender, User receiver) {
        Optional<FriendRequest> result = friendRequestRepository.findOneBySenderIdAndReceiverId(sender.getId(), receiver.getId());
        if (result.isPresent() && result.get().getStatus() == FriendStatus.BLOCKED)
            return;

        FriendRequest friendRequest = result.orElse(FriendRequest.builder().sender(sender).receiver(receiver).build());
        friendRequest.setStatus(FriendStatus.BLOCKED);
        friendRequestRepository.save(friendRequest);
    }
}
