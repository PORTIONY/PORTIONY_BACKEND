package com.portiony.portiony.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoSignupRequestDto implements SignupBaseDto {
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

    @Override
    public String getPassword() {
        return null;
    }
}
