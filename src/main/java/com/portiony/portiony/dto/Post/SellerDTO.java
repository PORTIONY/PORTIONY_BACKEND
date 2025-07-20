package com.portiony.portiony.dto.Post;

import lombok.*;

@Getter
@Setter
@Builder
public class SellerDTO {
    private Long sellerId;
    private String nickname;
    private String profileImage;
    private int saleCount;
    private int purchaseCount;
}
