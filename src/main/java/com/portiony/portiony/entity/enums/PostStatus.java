package com.portiony.portiony.entity.enums;

public enum PostStatus {
    PROGRESS("공구 중"),    // 모집 진행 중
    DONE("거래 완료"),       // 모집 완료
    CANCELLED("취소됨");     // 모집 취소

    private final String korean;

    PostStatus(String korean) {
        this.korean = korean;
    }

    public String toKorean() {
        return korean;
    }
}
