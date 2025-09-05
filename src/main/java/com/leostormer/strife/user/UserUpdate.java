package com.leostormer.strife.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdate {
    @Pattern(regexp = "^(?!.*\\.{2})(?!.*[_.]$)(?!.*^[_.]).*?[a-z0-9_.]{2,32}$")
    private String username;

    @Size(min = 8)
    private String password;

    @Email(message = "Email should be valid")
    private String email;

    private String profilePic;
}
