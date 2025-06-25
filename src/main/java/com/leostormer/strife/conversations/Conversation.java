package com.leostormer.strife.conversations;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "chats")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    @Id
    private ObjectId id;

    @DocumentReference(collection = "users", lazy = true)
    private User user1;

    @DocumentReference(collection = "users", lazy = true)
    private User user2;
}
