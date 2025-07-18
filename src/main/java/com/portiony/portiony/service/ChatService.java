package com.portiony.portiony.service;

import com.portiony.portiony.dto.Chat.ChatRequestDTO;
import com.portiony.portiony.dto.Chat.ChatResponseDTO;
import com.portiony.portiony.entity.ChatRoom;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.ChatRepository;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
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

}
