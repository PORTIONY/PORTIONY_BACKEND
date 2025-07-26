package com.portiony.portiony.dto.user;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class EditProfileRequest {
    private String nickname;
    private String email;
    private String currentPassword;
    private String newPassword;
    private MultipartFile profileImage;
}
