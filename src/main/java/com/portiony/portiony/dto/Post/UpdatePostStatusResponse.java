package com.portiony.portiony.dto.Post;

import lombok.Getter;
import com.portiony.portiony.entity.enums.PostStatus;

@Getter
public class UpdatePostStatusResponse {
    private final String message;
    private final String status;

    public UpdatePostStatusResponse(PostStatus postStatus) {
        if (postStatus == PostStatus.DONE) {
            this.message = "공구가 완료되었습니다.";
            this.status = "공구 완료";
        } else if (postStatus == PostStatus.PROGRESS) {
            this.message = "공구를 재개시하였습니다.";
            this.status = "공구 중";
        } else {
            this.message = "상태 변경됨";
            this.status = postStatus.name(); // "CANCELLED" 등
        }
    }
}
