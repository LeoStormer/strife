package com.leostormer.strife.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.message.Message;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.server.server_channel.ServerChannel;
import com.leostormer.strife.user.User;

public class ServerMessageTests extends ServerServiceTestSetup {
    @Autowired
    MessageRepository messageRepository;

    @NonNull
    @SuppressWarnings("null")
    ObjectId basicMemberMessageId;

    static final int NUM_MESSAGES_CHANNEL_2 = 10;

    @BeforeEach
    private void createExistingMessages() {
        ServerChannel channel1 = channelRepository.findServerChannelById(channel1Id).get();
        Message basicMemberMessage = new Message();
        basicMemberMessage.setSender(basicMemberUser);
        basicMemberMessage.setChannel(channel1);
        basicMemberMessage.setContent("Any content");
        basicMemberMessageId = messageRepository.save(basicMemberMessage).getId();

        ServerChannel channel2 = channelRepository.findServerChannelById(channel2Id).get();
        User[] usersTalking = new User[] { owner, moderator, basicMemberUser };
        for (int i = 0; i < NUM_MESSAGES_CHANNEL_2; i++) {
            Message message = new Message();
            message.setSender(usersTalking[i % usersTalking.length]);
            message.setChannel(channel2);
            message.setContent("Message number " + i);
            messageRepository.save(message);
        }

        ServerChannel adminOnlyChannel = channelRepository.findServerChannelById(adminOnlyPrivateChannelId).get();
        Message adminOnlyMessage = new Message();
        adminOnlyMessage.setSender(moderator);
        adminOnlyMessage.setChannel(adminOnlyChannel);
        adminOnlyMessage.setContent("Admin only content");
        messageRepository.save(adminOnlyMessage);
    }

    @AfterEach
    public void clearMessages() {
        messageRepository.deleteAll();
    }

    @Test
    public void shouldGetMessages() {
        MessageSearchOptions searchOptions = MessageSearchOptions.earliest();

        List<Message> adminOnlyMessages = serverService.getMessages(owner, existingServerId,
                adminOnlyPrivateChannelId, searchOptions);
        assertEquals(1, adminOnlyMessages.size());
        assertEquals(moderator.getId(), adminOnlyMessages.get(0).getSender().getId());

        List<Message> channel1Messages = serverService.getMessages(moderator, existingServerId, channel1Id,
                searchOptions);
        assertEquals(1, channel1Messages.size());
        assertEquals(basicMemberUser.getId(), channel1Messages.get(0).getSender().getId());

        List<Message> channel2Messages = serverService.getMessages(basicMemberUser, existingServerId, channel2Id,
                searchOptions);
        assertEquals(NUM_MESSAGES_CHANNEL_2, channel2Messages.size());
        assertEquals(owner.getId(), channel2Messages.get(0).getSender().getId());
    }

    @Test
    public void shouldNotGetMessagesIfCantViewChannel() {
        MessageSearchOptions searchOptions = MessageSearchOptions.earliest();
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getMessages(noPermissionsUser, existingServerId, channel1Id, searchOptions);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.getMessages(nonMemberUser, existingServerId, channel1Id, searchOptions);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getMessages(bannedUser, existingServerId, channel1Id, searchOptions);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getMessages(basicMemberUser, existingServerId, adminOnlyPrivateChannelId, searchOptions);
        });
    }

    @Test
    public void shouldSendMessage() {
        String messageContent = "A new message";
        Message message = serverService.sendMessage(basicMemberUser, existingServerId, channel1Id,
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
        String newMessageContent = "new content";
        Message oldMessage = messageRepository.findById(basicMemberMessageId).get();
        serverService.editMessage(basicMemberUser, existingServerId, basicMemberMessageId, newMessageContent);
        Optional<Message> newMessage = messageRepository.findById(basicMemberMessageId);
        assertTrue(newMessage.isPresent());
        assertEquals(oldMessage.getId(), newMessage.get().getId());
        assertEquals(oldMessage.getSender().getId(), newMessage.get().getSender().getId());
        assertEquals(oldMessage.getChannel().getId(), newMessage.get().getChannel().getId());
        assertEquals(newMessageContent, newMessage.get().getContent());
    }

    @Test
    public void shouldNotEditMessageIfNotSender() {
        String newMessageContent = "new content";
        Message oldMessage = messageRepository.findById(basicMemberMessageId).get();
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.editMessage(owner, existingServerId, basicMemberMessageId, newMessageContent);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.editMessage(moderator, existingServerId, basicMemberMessageId, newMessageContent);
        });
        Optional<Message> newMessage = messageRepository.findById(basicMemberMessageId);
        assertTrue(newMessage.isPresent());
        assertEquals(oldMessage.getId(), newMessage.get().getId());
        assertEquals(oldMessage.getSender().getId(), newMessage.get().getSender().getId());
        assertEquals(oldMessage.getChannel().getId(), newMessage.get().getChannel().getId());
        assertEquals(oldMessage.getContent(), newMessage.get().getContent());
    }

    @Test
    public void shouldDeleteMessageIfSender() {
        serverService.deleteMessage(basicMemberUser, existingServerId, channel1Id, basicMemberMessageId);
        assertFalse(messageRepository.existsById(basicMemberMessageId));
    }

    @Test
    public void shouldDeleteMessageIfHasPermission() {
        serverService.deleteMessage(moderator, existingServerId, channel1Id, basicMemberMessageId);
        assertFalse(messageRepository.existsById(basicMemberMessageId));
    }

    @Test
    public void shouldNotDeleteMessageWithoutPermission() {
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
