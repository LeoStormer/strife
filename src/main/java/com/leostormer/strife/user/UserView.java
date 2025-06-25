package com.leostormer.strife.user;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserView {
    private String id;
    private String username;
    private String profilePic;
    private Date createdDate;

    public UserView(User user) {
        this.id = user.getId().toString();
        this.username = user.getUsername();
        this.profilePic = user.getProfilePic();
        this.createdDate = user.getCreatedDate();
    }
}
