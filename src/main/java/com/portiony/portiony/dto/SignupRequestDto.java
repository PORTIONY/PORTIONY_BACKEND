package com.portiony.portiony.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDto implements SignupBaseDto {
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    private List<Long> agreementIds;
    private Long regionId;
    private Long subregionId;
    private Long dongId;
    private Integer mainCategory;
    private Integer purchaseReason;
    private Integer situation;
}
