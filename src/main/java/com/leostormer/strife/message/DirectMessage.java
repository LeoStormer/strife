package com.leostormer.strife.message;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.conversation.Conversation;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TypeAlias("DirectMessage")
@EqualsAndHashCode(callSuper = true)
public class DirectMessage extends Message {
    @DocumentReference(collection = "conversations", lazy = true)
    private Conversation conversation;

    public DirectMessage() {
        super();
    }
}
