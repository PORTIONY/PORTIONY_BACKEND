package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String nickname;
    private String profileImage;
    private int totalPurchases;
    private int totalSales;
    private double positiveRate;
    private boolean isMine;
}
