package com.portiony.portiony.dto.user;

import java.time.LocalDateTime;

public interface ReviewHistoryProjection {
    Long getPostId();
    Long getChatRoomId();
    Long getReviewId();
    Boolean getIsWritten();
    String getTitle();
    String getType();
    LocalDateTime getTransactionDate();
    Integer getChoice();
    String getContent();
}
