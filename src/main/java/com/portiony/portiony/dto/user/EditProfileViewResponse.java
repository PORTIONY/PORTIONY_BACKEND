package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

@Data
@AllArgsConstructor
@Builder
public class EditProfileViewResponse {
    private Long userId;
    private String nickname;
    private String email;
    private String profileImage;
}
