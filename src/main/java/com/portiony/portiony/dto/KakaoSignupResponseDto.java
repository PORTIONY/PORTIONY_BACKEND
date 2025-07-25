package com.portiony.portiony.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KakaoSignupResponseDto {
    private String accessToken;
    private String refreshToken;
    private Long userId;
}
