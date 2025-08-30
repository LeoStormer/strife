package com.leostormer.strife.message;

import java.util.Comparator;
import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "messages")
@Data
@TypeAlias("Message")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public abstract class Message {
    @Id
    private ObjectId id;

    private String content;

    @CreatedDate
    private Date timestamp;

    @DocumentReference(collection = "users", lazy = true)
    private User sender;

    public static Comparator<Message> sortByTimestampAscending = (m1, m2) -> m1.getTimestamp()
            .compareTo(m2.getTimestamp());
}
