package com.leostormer.strife.user;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.server.Server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private ObjectId id;

    private String username;

    private String password;

    private String email;

    private String profilePic;

    @CreatedDate
    private Date createdDate;

    @DocumentReference(collection = "servers")
    private List<Server> servers;

    @DocumentReference(collection = "conversations")
    private List<Conversation> conversations;
}
