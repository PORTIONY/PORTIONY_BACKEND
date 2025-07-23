package com.portiony.portiony.dto.comment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class CommentListResponse {
    Long totalCount;

    Long pageSize;
    Long totalPages;
    Long currentPage;

    Page<CommentDTO> items;

    public CommentListResponse(Long totalCount, Page<CommentDTO> items) {
        this.totalCount = totalCount;
        this.items = items;
        this.pageSize = (long) items.getSize();
        this.totalPages = (long) items.getTotalPages();
        this.currentPage = (long) items.getNumber();
    }
}