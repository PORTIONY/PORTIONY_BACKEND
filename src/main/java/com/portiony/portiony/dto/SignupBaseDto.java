package com.portiony.portiony.dto;

import java.util.*;

public interface SignupBaseDto {
    String getEmail();
    String getNickname();
    String getProfileImage();
    Long getRegionId();
    Long getSubregionId();
    Long getDongId();
    List<Long> getAgreementIds();
    Integer getMainCategory();
    Integer getPurchaseReason();
    Integer getSituation();
    String getPassword(); // 카카오용은 null 리턴하면 됨
}
