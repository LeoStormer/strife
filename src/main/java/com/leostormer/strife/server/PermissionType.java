package com.leostormer.strife.server;

public enum PermissionType {
    VIEW_CHANNELS(1L << 0),
    MANAGE_CHANNELS(1L << 1),
    MANAGE_ROLES(1L << 2),
    // CREATE_EXPRESSIONS(1L << 3),
    // MANAGE_EXPRESSIONS(1L << 4),
    // VIEW_AUDIT_LOG(1L << 5),
    // MANAGE_WEBHOOKS(1L << 6),
    MANAGE_SERVER(1L << 7),
    CREATE_INVITE(1L << 8),
    CHANGE_NICKNAME(1L << 9),
    MANAGE_NICKNAMES(1L << 10),
    KICK_MEMBERS(1L << 11),
    BAN_MEMBERS(1L << 12),
    TIMEOUT_MEMBERS(1L << 13),
    SEND_MESSAGES(1L << 14),
    MANAGE_MESSAGES(1L << 15),
    // SEND_MESSAGES_IN_THREADS(1L << 16),
    // CREATE_PUBLIC_THREADS(1L << 17),
    // CREATE_PRIVATE_THREADS(1L << 18),
    EMBED_LINKS(1L << 19),
    // ATTACH_FILES(1L << 20),
    ADD_REACTIONS(1L << 21),
    USE_EXTERNAL_EMOJIS(1L << 22),
    USE_EXTERNAL_STICKERS(1L << 23),
    MENTIONS(1L << 24),
    // MANAGE_THREADS(1L << 25)
    READ_MESSAGE_HISTORY(1L << 26),
    // SEND_TEXT_TO_SPEECH(1L << 27),
    // SEND_VOICE_MESSAGES(1L << 28),
    // CREATE_POLLS(1L << 29),
    // CONNECT_VOICE(1L << 30),
    // SPEAK_VOICE(1L << 31),
    // SHARE_VIDEO(1L << 32),
    // MUTE_MEMBERS(1L << 33),
    // DEAFEN_MEMBERS(1L << 34),
    // MOVE_MEMBERS(1L << 35),
    // USE_SOUNDBOARD(1L << 36),
    // USE_EXTERNAL_SOUNDS(1L << 37),
    // USE_VOICE_ACTIVITY(1L << 38),
    // PRIORIT_SPEAKER(1L << 39),
    // SET_VOICE_CHANNEL_STATUS(1L << 40),
    // USE_APPLICATION_COMMANDS(1L << 41),
    // USE_ACTIVITIES(1L << 42),
    // USE_EXTERNAL_APPS(1L << 43),
    // CREATE_EVENTS(1L << 44),
    // MANAGE_EVEENTS(1L << 45),
    ADMINISTRATOR(1L << 63);

    private final long value;

    public long getValue() {
        return value;
    }

    private PermissionType(long value) {
        this.value = value;
    }
}
