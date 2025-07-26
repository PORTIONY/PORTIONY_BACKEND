package com.portiony.portiony.util;

import com.portiony.portiony.entity.UserPreference;

public class UserPreferenceMapper {

    public static String getCategoryName(int code) {
        return switch (code) {
            case 1 -> "의료";
            case 2 -> "반려동물";
            case 3 -> "문구류";
            case 4 -> "육아용품";
            case 5 -> "화장품/뷰티";
            default -> "잡화/기타";
        };
    }

    public static String getPurchaseReason(int code) {
        return switch (code) {
            case 1 -> "꼭 필요한 것만 사는 소비자";
            case 2 -> "좋은 가격이면 뭐든 사고 싶은 소비자";
            case 3 -> "취미나 수집 목적으로 구매하는 소비자";
            default -> "다른 사람과 함께 나누는 걸 선호하는 소비자";
        };
    }

    public static String getSituation(int code) {
        return switch (code) {
            case 1 -> "자취 중인 1인 가구";
            case 2 -> "육아 중인 가족";
            case 3 -> "반려동물과 함께 사는 사람";
            case 4 -> "취미 생활을 즐기는 사람";
            default -> "대학생";
        };
    }
}
