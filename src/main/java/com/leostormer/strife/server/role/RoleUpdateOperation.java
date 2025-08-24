package com.leostormer.strife.server.role;

import java.util.List;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleUpdateOperation {
    /**
     * A list of new roles to add to the server.
     */
    private final List<Role> rolesToAdd;

    /**
     * A list of existing roles to remove from the server.
     */
    private final List<ObjectId> rolesToRemove;

    /**
     * A list of existing roles to chane properties of in the server.
     */
    private final List<Role> rolesToUpdate;
}
