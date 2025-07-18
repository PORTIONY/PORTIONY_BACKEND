package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRegisterRequest {

    private Double star;
    private Integer choice;
    private String content;
}
