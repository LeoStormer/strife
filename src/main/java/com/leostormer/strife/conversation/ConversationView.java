package com.leostormer.strife.conversation;

import com.leostormer.strife.user.UserView;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConversationView {
    private String id;
    private UserView user1;
    private UserView user2;
    private boolean locked;

    public ConversationView(Conversation conversation) {
        this(conversation.getId().toString(), new UserView(conversation.getUser1()),
                new UserView(conversation.getUser2()), conversation.isLocked());
    }
}
