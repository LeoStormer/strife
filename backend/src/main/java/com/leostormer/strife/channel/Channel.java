package com.leostormer.strife.channel;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.NonNull;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a channel where users can send messages.
 */
@Document(collection = "channels")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Channel {
    @Id
    @NonNull
    private ObjectId id;
}
