package com.leostormer.strife.server.member;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    // TODO: Move to own package and repository, acting as a join table between User and Server
    /**
     * Builds a <code>Member</code> from a <code>User</code>
     * 
     * @param user
     * @return the <code>Member</code>
     */
    public static Member fromUser(User user) {
        return Member.builder().userId(user.getId()).nickName(user.getUsername()).build();
    }

    /**
     * Builds a <code>Member</code> from a <code>User</code> with roles.
     * 
     * @param user
     * @param roles
     * @return the <code>Member</code>
     */
    public static Member fromUser(User user, Role... roles) {
        List<Role> roleList = List.of(roles).stream().sorted().toList();
        long permissions = Permissions.getPermissions(roleList);
        List<ObjectId> roleIds = roleList.stream().map(r -> r.getId()).toList();

        return Member.builder()
                .userId(user.getId())
                .nickName(user.getUsername())
                .roleIds(roleIds)
                .rolePriority(roleList.get(0).getPriority())
                .permissions(permissions)
                .build();
    }

    /**
     * Creates a banned member.
     * 
     * @param member
     * @param banReason
     * @return
     */
    public static Member createBannedMember(Member member, String banReason) {
        return Member.builder().userId(member.getUserId()).nickName(member.getNickName()).isBanned(true)
                .banReason(banReason).build();
    }

    private ObjectId userId;

    /**
     * The display name for the member in the server.
     */
    private String nickName;

    /**
     * The reason the user was banned.
     */
    private String banReason;

    /**
     * The priority of the highest role member has.
     */
    @Builder.Default
    private int rolePriority = 0;

    /**
     * The list of roles this member has. Is sorted from greatest priority to least.
     */
    @Builder.Default
    private List<ObjectId> roleIds = new ArrayList<ObjectId>();

    /**
     * Whether this member is banned from the server.
     */
    @Builder.Default
    private boolean isBanned = false;

    /**
     * Whether this member is the owner of the server.
     */
    @Builder.Default
    private boolean isOwner = false;

    /**
     * The accumulated permissions of all contained roles.
     */
    @Builder.Default
    private long permissions = 0L;
}
