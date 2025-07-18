package com.portiony.portiony.service;

import com.portiony.portiony.dto.Chat.ChatRequestDTO;
import com.portiony.portiony.dto.Chat.ChatResponseDTO;
import com.portiony.portiony.entity.ChatMessage;
import com.portiony.portiony.entity.ChatRoom;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.ChatMessageRopository;
import com.portiony.portiony.repository.ChatRepository;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatMessageRopository chatMessageRopository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    //채팅방 생성
    public ChatResponseDTO.CreateRoomRsDTO createChatRoom(ChatRequestDTO.CreateRoomRqDTO request){
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        User buyer = userRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new IllegalArgumentException("구매자 없음"));

        User seller = post.getUser();

        //본인 게시글 자신이 구매 x
        if (request.getBuyerId().equals(seller.getId())) {
            throw new IllegalArgumentException("본인 게시글 구매 불가");
        }

        //동일 채팅방 있는 지 검사
        ChatRoom chatRoom = chatRepository.findByPostIdAndBuyerId(request.getPostId(), request.getBuyerId())
                .orElseGet(() -> {
                    //없으면 새로 생성
                    ChatRoom newRoom = ChatRoom.builder()
                            .post(post)
                            .buyer(buyer)
                            .seller(seller)
                            .build();
                    return chatRepository.save(newRoom);
                });

        return ChatResponseDTO.CreateRoomRsDTO.builder()
                .chatRoomId(chatRoom.getId())
                .build();
    }

    //메시지 전송
    public ChatResponseDTO.ChatMessageRsDTO saveMessage(ChatRequestDTO.ChatMessageDTO dto) {
        ChatRoom room = chatRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("보낸 유저 없음"));

        User seller = room.getSeller(); //판매자
        User buyer = room.getBuyer(); //구매자

        // 보내는 사람이 판매자도 구매자도 아닌 경우
        if (!sender.getId().equals(seller.getId()) && !sender.getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("해당 채팅방의 참여자만 메시지를 보낼 수 있습니다.");
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(dto.getContent())
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRopository.save(message);

        return ChatResponseDTO.ChatMessageRsDTO.builder()
                .messageId(saved.getId())
                .senderId(sender.getId())
                .content(saved.getContent())
                .isRead(saved.isRead())
                .createdAt(saved.getCreatedAt())
                .build();
    }

}
