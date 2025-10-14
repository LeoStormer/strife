package com.leostormer.strife.user;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private ObjectId id;

    @NotEmpty
    @Pattern(regexp = "^(?!.*\\.{2})(?!.*[_.]$)(?!.*^[_.]).*?[a-z0-9_.]{2,32}$")
    private String username;

    @NotEmpty
    @Size(min = 8)
    private String password;

    @NotEmpty
    @Email
    private String email;

    private String profilePic;

    @CreatedDate
    private Date createdDate;

    @NonNull
    private Set<ObjectId> blockedUsers = new HashSet<>();

    @NonNull
    private Set<ObjectId> friends = new HashSet<>();

    public boolean hasBlocked(User user) {
        return blockedUsers.contains(user.getId());
    }

    public boolean hasBlocked(ObjectId userId) {
        return blockedUsers.contains(userId);
    }

    public boolean isFriend(User user) {
        return friends.contains(user.getId());
    }

    public boolean isFriend(ObjectId userId) {
        return friends.contains(userId);
    }
}
