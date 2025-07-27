package com.portiony.portiony.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDetailResponseDto {
    private Long regionId;
    private String regionName;
    private Long subregionId;
    private String subregionName;
    private Long dongId;
    private String dongName;
    private String address;
}
