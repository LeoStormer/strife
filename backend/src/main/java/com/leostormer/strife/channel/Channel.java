package com.leostormer.strife.channel;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * Represents a channel where users can send messages.
 */
@Document(collection = "channels")
@Data
public abstract class Channel {
    @Id
    private ObjectId id;
}
