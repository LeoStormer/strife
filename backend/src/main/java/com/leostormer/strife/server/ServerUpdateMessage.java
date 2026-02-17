package com.leostormer.strife.server;

import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerUpdateMessage {
    public enum UpdateType {
        SERVER_ADDED("SERVER_ADDED"),
        SERVER_REMOVED("SERVER_REMOVED");

        private final String value;

        public String getValue() {
            return value;
        }

        private UpdateType(String value) {
            this.value = value;
        }
    }


    @NonNull
    public static ServerUpdateMessage serverAdded(ServerView server) {
        return new ServerUpdateMessage(UpdateType.SERVER_ADDED, server, server.getId());
    }

    public static ServerUpdateMessage serverRemoved(String serverId) {
        return new ServerUpdateMessage(UpdateType.SERVER_REMOVED, null, serverId);
    }

    private final UpdateType type;
    private final ServerView server;
    private final String serverId;
}
