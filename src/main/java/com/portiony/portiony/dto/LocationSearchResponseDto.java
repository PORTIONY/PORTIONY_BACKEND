package com.portiony.portiony.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearchResponseDto {
    private Long regionId;
    private Long subregionId;
    private Long dongId;
    private String address;
}
