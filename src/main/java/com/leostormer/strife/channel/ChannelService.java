package com.leostormer.strife.channel;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leostormer.strife.server.Server;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChannelService {
    @Autowired
    private final ChannelRepository channelRepository;

    public Optional<Channel> getChannelById(ObjectId channelId) {
        return channelRepository.findById(channelId);
    }

    public Channel createChannel(Server server, String name, String category, String description, boolean isPublic) {
        Channel channel = new Channel();
        channel.setServer(server);
        channel.setName(name);
        channel.setCategory(category);
        channel.setDescription(description);
        channel.setPublic(isPublic);
        return channelRepository.save(channel);
    }

    public List<Channel> getChannelsByServer(ObjectId serverId) {
        return channelRepository.findAllByServerId(serverId);
    }

    public void updateChannelSettings(ObjectId channelId, ChannelUpdateOperation operation) {
        channelRepository.updateChannelSettings(channelId, operation);
    }

    public void deleteChannel(ObjectId channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException("Channel not found");
        }
        channelRepository.deleteById(channelId);
    }

    public void deleteAllByServer(ObjectId serverId) {
        channelRepository.deleteAllByServer(serverId);
    }

}
