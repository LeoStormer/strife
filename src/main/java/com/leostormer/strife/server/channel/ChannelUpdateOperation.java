package com.leostormer.strife.server.channel;

import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Update;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUpdateOperation {
    private String name;
    private String description;
    private String category;
    private Boolean isPublic;
    private Map<ObjectId, Long> userPermissions;
    private Map<ObjectId, Long> rolePermissions;

    public Update toUpdateObject() {
        Update update = new Update();
        if (name != null)
            update = update.set("name", name);

        if (description != null)
            update = update.set("description", description);

        if (category != null)
            update = update.set("category", category);

        if (isPublic != null)
            update = update.set("isPublic", isPublic.booleanValue());

        if (userPermissions != null)
            update = update.set("userPermissions", userPermissions);

        if (rolePermissions != null)
            update = update.set("rolePermissions", rolePermissions);

        return update;
    }
}
