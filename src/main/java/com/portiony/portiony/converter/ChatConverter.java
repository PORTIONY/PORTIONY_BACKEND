package com.portiony.portiony.converter;

import com.portiony.portiony.dto.ChatResponseDTO;
import com.portiony.portiony.entity.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChatConverter {
    // 채팅방 메시지 대화 내역 조회
    // entity > DTO
    public static ChatResponseDTO.GetMessageTotalRsDTO toDto(ChatMessage message) {
        return ChatResponseDTO.GetMessageTotalRsDTO.builder()
                .messageId(message.getId())
                .senderId(message.getSender().getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isRead(message.isRead())
                .imageUrls(
                        message.getChatImageList() == null ? Collections.emptyList() :
                                message.getChatImageList().stream()
                                        .map(ChatImage::getImageUrl)
                                        .collect(Collectors.toList())
                )
                .build();
    }

    // 메시지 엔티티 리스트 > DTO 리스트
    public static ChatResponseDTO.GetMessageTotalListDTO toDtoList(List<ChatMessage> messages, Long chatRoomId) {
        List<ChatResponseDTO.GetMessageTotalRsDTO> dtoList = messages.stream()
                .map(ChatConverter::toDto)
                .collect(Collectors.toList());

        return ChatResponseDTO.GetMessageTotalListDTO.builder()
                .chatRoomId(chatRoomId)
                .messageList(dtoList)
                .build();
    }

    //채팅방 목록 조회
    // 여러 개의 채팅방 리스트 변환
    public static ChatResponseDTO.ChatRoomListResponseDTO toChatRoomListDTO(List<ChatResponseDTO.ChatRoomPreviewDTO> previews) {
        return new ChatResponseDTO.ChatRoomListResponseDTO(previews);
    }

    // 개별 채팅방 DTO 변환
    public static ChatResponseDTO.ChatRoomPreviewDTO toChatRoomPreviewDTO(
            ChatRoom room,
            ChatMessage lastMessage,
            User partner,
            String postImageUrl,
            Boolean isSeller
    ) {
        Post post = room.getPost();

        return ChatResponseDTO.ChatRoomPreviewDTO.builder()
                .chatRoomId(room.getId())
                .lastMessageSenderId(lastMessage != null ? lastMessage.getSender().getId() : null)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .isRead(lastMessage != null ? lastMessage.isRead() : null)
                .partner(ChatResponseDTO.PartnerDTO.builder()
                        .partnerId(partner.getId())
                        .name(partner.getNickname())
                        .profileImageUrl(partner.getProfileImage())
                        .build())
                .post(ChatResponseDTO.PostDTO.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .imageUrl(postImageUrl)//이미지 처리
                        .price(post.getPrice())
                        .deadline(post.getDeadline())
                        .build())
                .status(ChatResponseDTO.StatusDTO.builder()
                        .sellerStatus(room.getSellerStatus())
                        .buyerStatus(room.getBuyerStatus())
                        .build())
                .isSeller(isSeller)
                .build();
    }
}
