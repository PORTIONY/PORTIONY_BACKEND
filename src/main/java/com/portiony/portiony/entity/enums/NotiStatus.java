package com.portiony.portiony.entity.enums;

public enum NotiStatus {
    DELIVERY_STARTED,      // 배송이 시작되었을 때 알림
    SALE_COMPLETED,        // 판매가 완료되었을 때 알림
    TRADE_COMPLETED,       // 거래가 완료되었을 때 알림
    FAVORITE_COMPLETED,    // 내가 찜한 게시물이 거래 완료되었을 때 알림
    FAVORITE_RECEIVED,     // 누군가 내 게시물을 찜했을 때 알림
    INQUIRY_ANSWERED,      // 문의에 대한 답변이 등록되었을 때 알림
    CHAT_TO_SELLER,        // 구매자가 판매자에게 채팅을 보냈을 때 알림
    CHAT_TO_BUYER          // 판매자가 구매자에게 채팅을 보냈을 때 알림
}