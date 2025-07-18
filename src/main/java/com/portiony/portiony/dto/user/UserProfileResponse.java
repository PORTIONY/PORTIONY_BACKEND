package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long userId;
    private String nickname;
    private String profileImage;
    private int purchasesCount;
    private int salesCount;
    private double positiveRate;
    private boolean isMine;
}
