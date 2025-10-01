package com.leostormer.strife.message;

import java.util.Date;

import com.leostormer.strife.user.UserView;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageView {
    private String id;
    private String content;
    private Date timestamp;
    private UserView sender;
    private String channelId;

    public MessageView(Message message) {
        this(message.getId().toString(), message.getContent(), message.getTimestamp(),
                new UserView(message.getSender()), message.getChannel().getId().toHexString());
    }
}
