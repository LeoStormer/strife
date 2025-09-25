package com.leostormer.strife.message;

import java.util.Comparator;
import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.channel.Channel;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "messages")
@Data
@TypeAlias("Message")
@NoArgsConstructor()
@AllArgsConstructor()
public class Message {
    @Id
    private ObjectId id;

    /**
     * The body of the message.
     */
    private String content;

    /**
     * The date this message was sent.
     */
    @CreatedDate
    private Date timestamp;

    /**
     * The {@link User} that sent this message.
     */
    @DocumentReference(collection = "users", lazy = true)
    private User sender;

    /**
     * The {@link Channel} that this message was sent in.
     */
    @DocumentReference(collection = "channels", lazy = true)
    private Channel channel;

    public static Comparator<Message> sortByTimestampAscending = (m1, m2) -> m1.getTimestamp()
            .compareTo(m2.getTimestamp());
}
