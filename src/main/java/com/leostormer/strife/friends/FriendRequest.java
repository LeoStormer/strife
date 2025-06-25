package com.leostormer.strife.friends;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "friend_requests")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FriendRequest {
    @Id
    private ObjectId id;

    @DocumentReference(collection = "users")
    private User sender;

    @DocumentReference(collection = "users")
    private User receiver;

    @Builder.Default
    private FriendStatus status = FriendStatus.PENDING;
}
