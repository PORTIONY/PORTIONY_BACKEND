package com.portiony.portiony.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KakaoSignupRequestDto {
    private String email;
    private String nickname;
    private String profileImage;
    private Long regionId;
    private Long subregionId;
    private Long dongId;
    private List<Long> agreementIds;
    private Integer mainCategory;
    private Integer purchaseReason;
    private Integer situation;
}
