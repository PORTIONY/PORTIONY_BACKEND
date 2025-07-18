package com.portiony.portiony.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDto {
    private String email;
    private String password;
    private String nickname;
    private String profileImage;

    private List<Long> agreementIds;

    private Long regionId;
    private Long subregionId;
    private Long dongId;

    // 사용자 선호정보 필드 추가
    private Integer mainCategory;
    private Integer purchaseReason;
    private Integer situation;

}
