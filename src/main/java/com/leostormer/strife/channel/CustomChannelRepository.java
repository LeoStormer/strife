package com.leostormer.strife.channel;

import org.bson.types.ObjectId;

public interface CustomChannelRepository {
    public void updateChannelSettings(ObjectId channelId, ChannelUpdateOperation operation);
}
