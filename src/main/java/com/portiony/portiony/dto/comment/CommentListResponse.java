package com.portiony.portiony.dto.comment;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommentListResponse {
    Long totalCount;
    List<CommentDTO> items;
}