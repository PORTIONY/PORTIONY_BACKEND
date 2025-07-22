package com.portiony.portiony.controller;

import com.portiony.portiony.dto.LocationRequestDto;
import com.portiony.portiony.dto.LocationResponseDto;
import com.portiony.portiony.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/resolve")
    public LocationResponseDto resolveLocation(@RequestBody LocationRequestDto request) {
        return locationService.resolveRegionIds(request.getLatitude(), request.getLongitude());
    }
}