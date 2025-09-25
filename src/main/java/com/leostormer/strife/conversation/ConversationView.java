package com.leostormer.strife.conversation;

import java.util.List;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConversationView {
    private String id;
    List<String> userids;
    private boolean locked;

    public ConversationView(Conversation conversation) {
        this(conversation.getId().toString(),
                conversation.getUserPresenceMap().keySet().stream().map(ObjectId::toHexString).toList(),
                conversation.isLocked());
    }
}
