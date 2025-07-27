package com.portiony.portiony.dto.location;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResolvedLocationResponseDto {
    private String currentAddress;
    private List<LocationSearchResponseDto> results;
}