package com.leostormer.strife.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Relationship {
    public enum RelationshipType {
        FRIEND, BLOCKED, PENDING, PENDING_OTHER;
    }

    private RelationshipType type;
    private UserView user;
    private String requestId;
}
