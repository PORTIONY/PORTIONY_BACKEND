package com.portiony.portiony.controller;

import com.portiony.portiony.dto.LocationDetailResponseDto;
import com.portiony.portiony.dto.LocationRequestDto;
import com.portiony.portiony.dto.LocationResponseDto;
import com.portiony.portiony.dto.LocationSearchResponseDto;
import com.portiony.portiony.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/resolve")
    public List<LocationSearchResponseDto> resolveLocation(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){
        return locationService.resolveRegionIds(latitude, longitude, page, size);
    }

    @GetMapping("/search")
    public List<LocationSearchResponseDto> searchLocation(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return locationService.searchLocations(keyword, page, size);
    }

    @GetMapping("/{dongId}")
    public ResponseEntity<LocationDetailResponseDto> getAddress(@PathVariable Long dongId) {
        return ResponseEntity.ok(locationService.getByDongId(dongId));
    }
}