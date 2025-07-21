package com.portiony.portiony.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CommentListResponse {
    Long totalCount;
    List<CommentDTO> items;
}