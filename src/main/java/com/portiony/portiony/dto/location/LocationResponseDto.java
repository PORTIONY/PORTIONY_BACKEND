package com.portiony.portiony.dto.location;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationResponseDto {
    private Long regionId;
    private Long subregionId;
    private Long dongId;
    private String address;
}