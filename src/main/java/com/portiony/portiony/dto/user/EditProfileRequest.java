package com.portiony.portiony.dto.user;

import lombok.Getter;

@Getter
public class EditProfileRequest {
    private String nickname;
    private String email;
    private String currentPassword;
    private String newPassword;
}
