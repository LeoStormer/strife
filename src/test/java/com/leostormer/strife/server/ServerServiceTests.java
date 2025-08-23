package com.leostormer.strife.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.leostormer.strife.channel.Channel;
import com.leostormer.strife.channel.ChannelUpdateOperation;
import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.message.ChannelMessage;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.server.member.Member;

@SpringBootTest
@ActiveProfiles("test")
public class ServerServiceTests extends ServerServiceTestSetup {
    @Autowired
    MessageRepository messageRepository;

    ObjectId basicMemberMessageId;

    private void createExistingMessages() {
        Channel channel1 = channelRepository.findById(channel1Id).get();
        ChannelMessage basicMemberMessage = new ChannelMessage();
        basicMemberMessage.setSender(basicMemberUser);
        basicMemberMessage.setChannel(channel1);
        basicMemberMessage.setContent("Any content");
        basicMemberMessageId = messageRepository.save(basicMemberMessage).getId();
    }

    @AfterEach
    public void clearMessages() {
        messageRepository.deleteAll();
    }

    @Test
    public void shouldCreateServer() {
        String serverName = "New Server";
        String serverDescription = "A New Server";
        Server server = serverService.createServer(owner, serverName, serverDescription);
        assertTrue(serverRepository.existsById(server.getId()));
        assertEquals(server.getName(), serverName);
        assertEquals(server.getDescription(), serverDescription);
        assertEquals(server.getOwner().getId(), owner.getId());
    }

    @Test
    public void shouldDeleteServerIfOwner() {
        serverService.deleteServer(owner, existingServerId);
        assertFalse(serverRepository.existsById(existingServerId));
        List<Channel> channels = channelRepository.findAllByServerId(existingServerId);
        assertTrue(channels.size() == 0);
    }

    @Test
    public void shouldNotDeleteServerIfNotOwner() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(basicMemberUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(noPermissionsUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(nonMemberUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(bannedUser, existingServerId);
        });
        assertTrue(serverRepository.existsById(existingServerId));
    }

    @Test
    public void shouldUpdateServerDetails() {
        String newName = "NewServerName";
        String newDescription = "A new server description";

        serverService.updateServerDetails(owner, existingServerId, newName, newDescription);
        Server server = serverRepository.findById(existingServerId).get();
        assertEquals(newName, server.getName());
        assertEquals(newDescription, server.getDescription());
    }

    @Test
    public void shouldUpdateServerDetailsPartially() {
        String newName = "NewServerName";

        Server server = serverRepository.findById(existingServerId).get();
        serverService.updateServerDetails(owner, existingServerId, newName, null);
        Server updatedServer = serverRepository.findById(existingServerId).get();
        assertEquals(newName, updatedServer.getName());
        assertEquals(server.getDescription(), updatedServer.getDescription());
    }


    @Test
    public void shouldNotUpdateServerDetailsWithoutPermisison() {
        String newName = "NewServerName";
        String newDescription = "A new server description";

        Server server = serverRepository.findById(existingServerId).get();
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateServerDetails(basicMemberUser, existingServerId, newName, newDescription);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.updateServerDetails(nonMemberUser, existingServerId, newName, newDescription);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateServerDetails(noPermissionsUser, existingServerId, newName, newDescription);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateServerDetails(bannedUser, existingServerId, newName, newDescription);
        });

        Server updatedServer = serverRepository.findById(existingServerId).get();
        assertEquals(server.getName(), updatedServer.getName());
        assertEquals(server.getDescription(), updatedServer.getDescription());
    }

    @Test
    public void shouldTransferServerOwnerShip() {
        serverService.transferServerOwnership(owner, moderator, existingServerId);
        Server server = serverRepository.findById(existingServerId).get();
        Member modMember = server.getMember(moderator).get();
        Member oldOwner = server.getMember(owner).get();
        assertEquals(moderator.getId(), server.getOwner().getId());
        assertTrue(modMember.isOwner());
        assertFalse(oldOwner.isOwner());
    }

    @Test
    public void shouldNotTransferServerOwnershipIfNotOwner() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(moderator, moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(basicMemberUser, moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(noPermissionsUser, moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(nonMemberUser, moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(bannedUser, moderator, existingServerId);
        });

        Server server = serverRepository.findById(existingServerId).get();
        assertEquals(owner.getId(), server.getOwner().getId());
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

        Channel updatedChannel = channelRepository.findById(channel1Id).get();
        assertEquals(channel.getName(), updatedChannel.getName());
        assertEquals(channel.getDescription(), updatedChannel.getDescription());
        assertEquals(channel.getCategory(), updatedChannel.getCategory());
        assertEquals(channel.isPublic(), updatedChannel.isPublic());
        assertEquals(channel.getRolePermissions(), updatedChannel.getRolePermissions());
        assertEquals(channel.getUserPermissions(), updatedChannel.getUserPermissions());
    }

    @Test
    public void shouldSendMessage() {
        String messageContent = "A new message";
        ChannelMessage message = serverService.sendMessage(basicMemberUser, existingServerId, channel1Id,
                messageContent);
        assertTrue(messageRepository.existsById(message.getId()));
        assertEquals(basicMemberUser.getId(), message.getSender().getId());
        assertEquals(channel1Id, message.getChannel().getId());
        assertEquals(messageContent, message.getContent());
    }

    @Test
    public void shouldNotSendMessageWithoutPermisison() {
        String messageContent = "Any message content.";
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.sendMessage(noPermissionsUser, existingServerId, channel1Id, messageContent);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.sendMessage(nonMemberUser, existingServerId, channel1Id, messageContent);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.sendMessage(bannedUser, existingServerId, channel1Id, messageContent);
        });
    }

    @Test
    public void shouldNotSendMessageIfBanned() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.sendMessage(bannedUser, existingServerId, channel1Id, "Any message");
        });
    }

    @Test
    public void shouldEditMessageIfSender() {
        createExistingMessages();
        String newMessageContent = "new content";
        ChannelMessage oldMessage = messageRepository.findChannelMessageById(basicMemberMessageId).get();
        serverService.editMessage(basicMemberUser, existingServerId, basicMemberMessageId, newMessageContent);
        Optional<ChannelMessage> newMessage = messageRepository.findChannelMessageById(basicMemberMessageId);
        assertTrue(newMessage.isPresent());
        assertEquals(oldMessage.getId(), newMessage.get().getId());
        assertEquals(oldMessage.getSender().getId(), newMessage.get().getSender().getId());
        assertEquals(oldMessage.getChannel().getId(), newMessage.get().getChannel().getId());
        assertEquals(newMessageContent, newMessage.get().getContent());
    }

    @Test
    public void shouldNotEditMessageIfNotSender() {
        createExistingMessages();
        String newMessageContent = "new content";
        ChannelMessage oldMessage = messageRepository.findChannelMessageById(basicMemberMessageId).get();
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.editMessage(owner, existingServerId, basicMemberMessageId, newMessageContent);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.editMessage(moderator, existingServerId, basicMemberMessageId, newMessageContent);
        });
        Optional<ChannelMessage> newMessage = messageRepository.findChannelMessageById(basicMemberMessageId);
        assertTrue(newMessage.isPresent());
        assertEquals(oldMessage.getId(), newMessage.get().getId());
        assertEquals(oldMessage.getSender().getId(), newMessage.get().getSender().getId());
        assertEquals(oldMessage.getChannel().getId(), newMessage.get().getChannel().getId());
        assertEquals(oldMessage.getContent(), newMessage.get().getContent());
    }

    @Test
    public void shouldDeleteMessageIfSender() {
        createExistingMessages();
        serverService.deleteMessage(basicMemberUser, existingServerId, channel1Id, basicMemberMessageId);
        assertFalse(messageRepository.existsById(basicMemberMessageId));
    }

    @Test
    public void shouldDeleteMessageIfHasPermission() {
        createExistingMessages();
        serverService.deleteMessage(moderator, existingServerId, channel1Id, basicMemberMessageId);
        assertFalse(messageRepository.existsById(basicMemberMessageId));
    }

    @Test
    public void shouldNotDeleteMessageWithoutPermission() {
        createExistingMessages();
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteMessage(noPermissionsUser, existingServerId, channel1Id, basicMemberMessageId);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.deleteMessage(nonMemberUser, existingServerId, channel1Id, basicMemberMessageId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteMessage(bannedUser, existingServerId, channel1Id, basicMemberMessageId);
        });
        assertTrue(messageRepository.existsById(basicMemberMessageId));
    }
}
