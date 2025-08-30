package com.leostormer.strife.server.channel;

import java.util.List;

import org.bson.types.ObjectId;

import com.leostormer.strife.server.member.Member;

public interface CustomChannelRepository {
    public void updateChannelSettings(ObjectId channelId, ChannelUpdateOperation operation);

    public List<Channel> getVisibleChannels(ObjectId serverId, Member member);
}
