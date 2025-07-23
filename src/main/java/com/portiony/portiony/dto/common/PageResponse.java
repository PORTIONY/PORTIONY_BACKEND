package com.portiony.portiony.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private long total;
    private int page;
    private List<T> post;
}
