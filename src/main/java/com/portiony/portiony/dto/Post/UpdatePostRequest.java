package com.portiony.portiony.dto.Post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class UpdatePostRequest {

    @NotBlank(message = "제목 입력은 필수입니다.")
    private String title;

    @NotBlank(message = "내용 입력은 필수입니다.")
    private String description;

    @NotNull(message = "수량 입력은 필수입니다.")
    private int capacity;

    @NotNull(message = "가격 입력은 필수입니다.")
    private int price;

    @NotNull(message = "소분 수량 입력은 필수입니다.")
    private int unitAmount; // 소분량 필드 추가

    @NotBlank(message = "단위 입력은 필수입니다.")
    private String unit;

    @NotNull(message = "마감일 입력은 필수입니다.")
    private LocalDateTime deadline;

    @NotBlank(message = "배송 방법 입력은 필수입니다.")
    private String deliveryMethod;

    @NotEmpty(message = "이미지 등록은 필수입니다.")
    private List<String> images;
}
