package com.portiony.portiony.service;

import com.portiony.portiony.dto.ChatRequestDTO;
import com.portiony.portiony.dto.ChatResponseDTO;
import com.portiony.portiony.entity.ChatMessage;
import com.portiony.portiony.entity.ChatRoom;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.ChatMessageRepository;
import com.portiony.portiony.repository.ChatRoomRepository;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    //공통 에러처리 로직 / 객체 불러옴
    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    private ChatRoom getChatRoomOrThrow(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
    }

    //채팅방 생성
    public ChatResponseDTO.CreateRoomRsDTO createChatRoom(ChatRequestDTO.CreateRoomRqDTO request){
        Post post = getPostOrThrow(request.getPostId());
        User buyer = getUserOrThrow(request.getBuyerId());
        User seller = post.getUser();

        //본인 게시글 자신이 구매 x
        if (request.getBuyerId().equals(seller.getId())) {
            throw new IllegalArgumentException("본인 게시글 구매 불가");
        }

        //동일 채팅방 있는 지 검사
        ChatRoom chatRoom = chatRoomRepository.findByPostIdAndBuyerId(request.getPostId(), request.getBuyerId())
                .orElseGet(() -> {
                    //없으면 새로 생성
                    ChatRoom newRoom = ChatRoom.builder()
                            .post(post)
                            .buyer(buyer)
                            .seller(seller)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return ChatResponseDTO.CreateRoomRsDTO.builder()
                .chatRoomId(chatRoom.getId())
                .build();
    }

    //메시지 전송 +)이미지 처리 로직 추가 필요
    public ChatResponseDTO.ChatMessageRsDTO saveMessage(ChatRequestDTO.ChatMessageDTO dto) {
        ChatRoom room = getChatRoomOrThrow(dto.getChatRoomId());
        User sender = getUserOrThrow(dto.getSenderId());

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

        ChatMessage saved = chatMessageRepository.save(message);

        return ChatResponseDTO.ChatMessageRsDTO.builder()
                .chatRoomId(room.getId())
                .messageId(saved.getId())
                .senderId(sender.getId())
                .content(saved.getContent())
                .isRead(saved.isRead())
                .createdAt(saved.getCreatedAt())
                .build();
    }


    //메시지 읽음 처리
    public void markMessagesAsRead(Long chatRoomId, Long userId) {
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new IllegalArgumentException("존재하지 않는 채팅방입니다.");
        }

        //내가 보낸 메시지가 아닌 것들 중에서 read > false인 메시지들
        List<ChatMessage> unreadMessages =
                chatMessageRepository.findByChatRoomIdAndSenderIdNotAndIsReadFalse(chatRoomId, userId);

        //read > true 처리
        List<ChatMessage> updatedMessages = unreadMessages.stream()
                .peek(msg -> msg.setRead(true))
                .toList();

        chatMessageRepository.saveAll(updatedMessages);
    }

}
