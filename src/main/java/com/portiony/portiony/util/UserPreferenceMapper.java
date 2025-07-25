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
            case 1 -> "꼭 필요한 것만 사요";
            case 2 -> "좋은 딜이면 뭐든 좋아요";
            case 3 -> "취미나 수집용이에요";
            default -> "다른 사람과 나눌 수 있다면 더 좋아요";
        };
    }

    public static String getSituation(int code) {
        return switch (code) {
            case 1 -> "자취 중인 1인 가구";
            case 2 -> "육아 중인 가구";
            case 3 -> "강아지/고양이와 함께 살아요";
            case 4 -> "취미 생활을 즐겨요";
            default -> "학생이에요";
        };
    }

    public static String toGeminiPrompt(UserPreference pref) {
        StringBuilder sb = new StringBuilder();

        sb.append("이 사용자는 ");

        if (pref.getPurchaseReason() != 0) {
            sb.append("‘")
                    .append(getPurchaseReason(pref.getPurchaseReason()))
                    .append("’의 이유로 구매하고 싶어하고, ");
        }

        if (pref.getSituation() != 0) {
            sb.append("‘")
                    .append(getSituation(pref.getSituation()))
                    .append("’ 상황에 어울리는 상품을 찾고 있어요. ");
        }

        if (pref.getMainCategory() != 0) {
            sb.append("이 사용자는 ‘")
                    .append(getCategoryName(pref.getMainCategory()))
                    .append("’ 카테고리를 선호해요. ");
        }

        sb.append("이 조건에 어울리는 공동구매 상품 10개만 골라줘. 결과는 번호만 알려줘. 예: 1, 3, 5");

        return sb.toString();
    }
}
