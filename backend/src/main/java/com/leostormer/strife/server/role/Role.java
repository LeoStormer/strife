package com.leostormer.strife.server.role;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role implements Comparable<Role> {
    @Id
    private ObjectId id;

    private String name;

    private int priority;

    private long permissions;

    @Override
    public int compareTo(Role other) {
        // Order roles by priority in descending order
        return -1 * Integer.compare(this.priority, other.priority);
    }
}
