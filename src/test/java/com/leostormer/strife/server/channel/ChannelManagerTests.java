package com.leostormer.strife.server.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.ServerServiceTestSetup;

public class ChannelManagerTests extends ServerServiceTestSetup {
    @Test
    public void shouldGetChannels() {
        List<Channel> channels = serverService.getChannels(owner, existingServerId);
        assertEquals(3, channels.size());
        channels = serverService.getChannels(moderator, existingServerId);
        assertEquals(3, channels.size());
        channels = serverService.getChannels(basicMemberUser, existingServerId);
        assertEquals(2, channels.size());
        assertTrue(channels.stream().noneMatch(c -> c.getId().equals(adminOnlyPrivateChannelId)));
    }

    @Test
    public void shouldNotGetChannelsIfNotMember() {
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.getChannels(nonMemberUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getChannels(noPermissionsUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getChannels(bannedUser, existingServerId);
        });
    }

    @Test
    public void shouldAddChannel() {
        Channel channel1 = serverService.addChannel(owner, existingServerId, "OwnerChannel", "Test", "Owners Channel",
                false);
        assertTrue(channelRepository.existsById(channel1.getId()));
        Channel channel2 = serverService.addChannel(moderator, existingServerId, "ModeratorChannel", "Test",
                "Mod channel", false);
        assertTrue(channelRepository.existsById(channel2.getId()));
    }

    @Test
    public void shouldNotAddChannelWithoutPermission() {
        String channelName = "TesChannel";
        String channelDescription = "A test channel";
        String channelCategory = "Test";
        boolean isPublic = false;
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.addChannel(basicMemberUser, existingServerId, channelName, channelCategory,
                    channelDescription, isPublic);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.addChannel(noPermissionsUser, existingServerId, channelName, channelCategory,
                    channelDescription, isPublic);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.addChannel(nonMemberUser, existingServerId, channelName, channelCategory, channelDescription,
                    isPublic);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.addChannel(bannedUser, existingServerId, channelName, channelCategory, channelDescription,
                    isPublic);
        });
    }

    @Test
    public void shouldRemoveChannel() {
        serverService.removeChannel(owner, existingServerId, channel1Id);
        assertFalse(channelRepository.existsById(channel1Id));
        serverService.removeChannel(moderator, existingServerId, channel2Id);
        assertFalse(channelRepository.existsById(channel2Id));
    }

    @Test
    public void shouldNotRemoveChannelWithoutPermisison() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.removeChannel(basicMemberUser, existingServerId, channel1Id);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.removeChannel(noPermissionsUser, existingServerId, channel1Id);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.removeChannel(nonMemberUser, existingServerId, channel1Id);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.removeChannel(bannedUser, existingServerId, channel1Id);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.removeChannel(moderator, existingServerId, adminOnlyPrivateChannelId);
        });

        assertTrue(channelRepository.existsById(channel1Id));
    }

    @Test
    public void shouldUpdateSpecifiedChannelSettingsAndKeepUnspecified() {
        String newName = "newChannelName";
        String newDescription = "New channel description";
        String newCategory = "NewCategory";
        ChannelUpdateOperation operation = new ChannelUpdateOperation();
        operation.setName(newName);
        operation.setDescription(newDescription);
        operation.setCategory(newCategory);

        Channel channel = channelRepository.findById(channel1Id).get();
        serverService.updateChannelSettings(moderator, existingServerId, channel1Id, operation);
        Channel updatedChannel = channelRepository.findById(channel1Id).get();
        assertEquals(newName, updatedChannel.getName());
        assertEquals(newDescription, updatedChannel.getDescription());
        assertEquals(newCategory, updatedChannel.getCategory());
        assertEquals(channel.isPublic(), updatedChannel.isPublic());
        assertEquals(channel.getRolePermissions(), updatedChannel.getRolePermissions());
        assertEquals(channel.getUserPermissions(), updatedChannel.getUserPermissions());
    }

    @Test
    public void shouldNotUpdateChannelSettingsWithoutPermission() {
        String newName = "newChannelName";
        String newDescription = "New channel description";
        String newCategory = "NewCategory";
        ChannelUpdateOperation operation = new ChannelUpdateOperation();
        operation.setName(newName);
        operation.setDescription(newDescription);
        operation.setCategory(newCategory);

        Channel channel = channelRepository.findById(channel1Id).get();
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateChannelSettings(basicMemberUser, existingServerId, channel1Id, operation);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateChannelSettings(noPermissionsUser, existingServerId, channel1Id, operation);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.updateChannelSettings(nonMemberUser, existingServerId, channel1Id, operation);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateChannelSettings(bannedUser, existingServerId, channel1Id, operation);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateChannelSettings(moderator, existingServerId, adminOnlyPrivateChannelId, operation);
        });

        Channel updatedChannel = channelRepository.findById(channel1Id).get();
        assertEquals(channel.getName(), updatedChannel.getName());
        assertEquals(channel.getDescription(), updatedChannel.getDescription());
        assertEquals(channel.getCategory(), updatedChannel.getCategory());
        assertEquals(channel.isPublic(), updatedChannel.isPublic());
        assertEquals(channel.getRolePermissions(), updatedChannel.getRolePermissions());
        assertEquals(channel.getUserPermissions(), updatedChannel.getUserPermissions());
    }
}
