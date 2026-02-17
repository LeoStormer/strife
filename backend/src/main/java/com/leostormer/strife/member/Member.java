package com.leostormer.strife.member;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.lang.NonNull;

import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndex(name = "server_user_idx", def = "{'server': 1, 'user': 1}", unique = true)
@CompoundIndex(name = "server_user_isBanned_idx", def = "{'server': 1, 'user': 1, 'isBanned': 1}", unique = true)
@CompoundIndex(name = "user_isBanned_idx", def = "{'user': 1, 'isBanned': 1}")
public class Member {
    /**
     * Builds a <code>Member</code> from a <code>User</code>
     * 
     * @param user
     * @return the <code>Member</code>
     */
    @NonNull
    public static Member from(User user, Server server) {
        return new Member(user, server);
    }

    /**
     * Builds a <code>Member</code> from a <code>User</code> with roles.
     * 
     * @param user
     * @param roles
     * @return the <code>Member</code>
     */
    @NonNull
    public static Member from(User user, Server server, Role... roles) {
        return new Member(user, server, roles);
    }

    /**
     * Creates a banned member.
     * 
     * @param member
     * @param banReason
     * @return
     */
    @NonNull
    public static Member createBannedMember(Member member, String banReason) {
        return createBannedMember(member.getUser(), member.getServer(), banReason);
    }

    /**
     * Creates a banned member.
     * 
     * @param member
     * @param banReason
     * @return
     */
    @NonNull
    public static Member createBannedMember(User user, Server server, String banReason) {
        Member newMember = new Member(user, server);
        newMember.setBanned(true);
        newMember.setBanReason(banReason);
        return newMember;
    }

    // MongoDb automatically generates IDs
    @SuppressWarnings("null")
    public Member(User user, Server server) {
        this.user = user;
        this.server = server;
        this.nickname = user.getUsername();
        this.isOwner = server.getOwner().getId().equals(user.getId());
    }

    @SuppressWarnings("null")
    public Member(User user, Server server, Role... roles) {
        List<Role> roleList = List.of(roles).stream().sorted().toList();
        long permissions = Permissions.getPermissions(roleList);
        List<ObjectId> roleIds = roleList.stream().map(r -> r.getId()).toList();

        this.user = user;
        this.server = server;
        this.nickname = user.getUsername();
        this.rolePriority = roleList.get(0).getPriority();
        this.roleIds = roleIds;
        this.permissions = permissions;
        this.isOwner = server.getOwner().getId().equals(user.getId());
    }

    @Id
    @NonNull
    private ObjectId id;

    @DocumentReference(collection = "users", lazy = true)
    private User user;

    @DocumentReference(collection = "servers", lazy = true)
    private Server server;

    /**
     * The display name for the member in the server.
     */
    private String nickname;

    /**
     * The reason the user was banned.
     */
    private String banReason;

    /**
     * The priority of the highest role member has.
     */
    private int rolePriority = 0;

    /**
     * The list of roles this member has. Is sorted from greatest priority to least.
     */
    private List<ObjectId> roleIds = new ArrayList<ObjectId>();

    /**
     * Whether this member is banned from the server.
     */
    private boolean isBanned = false;

    /**
     * Whether this member is the owner of the server.
     */
    private boolean isOwner = false;

    /**
     * The accumulated permissions of all contained roles.
     */
    private long permissions = 0L;
}
