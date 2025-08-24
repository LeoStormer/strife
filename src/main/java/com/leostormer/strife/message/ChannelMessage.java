package com.leostormer.strife.message;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.channel.Channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TypeAlias("ChannelMessage")
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ChannelMessage extends Message {
    @DocumentReference(collection = "channels", lazy = true)
    private Channel channel;

    public ChannelMessage() {
        super();
    }
}
