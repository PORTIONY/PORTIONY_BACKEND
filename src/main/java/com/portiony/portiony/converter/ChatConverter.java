package com.portiony.portiony.converter;

import com.portiony.portiony.dto.ChatResponseDTO;
import com.portiony.portiony.entity.ChatImage;
import com.portiony.portiony.entity.ChatMessage;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChatConverter {
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
}
