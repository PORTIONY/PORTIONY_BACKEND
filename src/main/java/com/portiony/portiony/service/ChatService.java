package com.portiony.portiony.service;

//import com.portiony.portiony.converter.ChatConverter;
import com.portiony.portiony.converter.ChatConverter;
import com.portiony.portiony.dto.ChatRequestDTO;
import com.portiony.portiony.dto.ChatResponseDTO;
import com.portiony.portiony.entity.*;
import com.portiony.portiony.entity.enums.ChatStatus;
import com.portiony.portiony.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostImageRepository postImageRepository;

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
        // 이미지, content 둘 다 없으면 에러처리
        if ((dto.getContent() == null || dto.getContent().isBlank()) &&
                (dto.getImageUrls() == null || dto.getImageUrls().isEmpty())) {
            throw new IllegalArgumentException("content나 이미지는 하나 이상 포함되어야 합니다.");
        }

        //메시지 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(dto.getContent())
                .isRead(false)
                .build();

        //이미지가 있으면 처리
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            for (String imageUrl : dto.getImageUrls()) {
                ChatImage image = ChatImage.builder()
                        .imageUrl(imageUrl)
                        .build();
                message.addChatImage(image); //연관관계 편의 메서드 사용 / 양쪽 엔티티 매핑
            }
        }

        ChatMessage saved = chatMessageRepository.save(message);

        //image url 목록 추출
        List<String> imageUrls = saved.getChatImageList().stream()
                .map(ChatImage::getImageUrl)
                .collect(Collectors.toList());

        return ChatResponseDTO.ChatMessageRsDTO.builder()
                .chatRoomId(room.getId())
                .messageId(saved.getId())
                .senderId(sender.getId())
                .content(saved.getContent())
                .imageUrls(imageUrls)
                .isRead(saved.isRead())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    //메시지 처리 - 외부 스토리지 이미지 업로드 + 추후 로직 추가 필요함
    public List<String> uploadImages(Long chatRoomId, Long userId, List<MultipartFile> images) {
        ChatRoom room = getChatRoomOrThrow(chatRoomId);

        if (!room.getSeller().getId().equals(userId) && !room.getBuyer().getId().equals(userId)) {
            throw new IllegalArgumentException("채팅방 참여자만 업로드 가능합니다.");
        }
        //s3에 올려야함 추후 로직 추가 필요
//        return images.stream()
//                .map(s3Service::upload)
//                .collect(Collectors.toList());
        //임시 URL 생성해서 반환
        return images.stream()
                .map(image -> "https://fake-url.com/chat-images/" + UUID.randomUUID() + "_" + image.getOriginalFilename())
                .collect(Collectors.toList());
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

    //거래 완료 처리
    public ChatResponseDTO.ChatCompleteRsDTO chatToComplete(Long chatRoomId, Long userId) {
        ChatRoom room = getChatRoomOrThrow(chatRoomId);

        boolean isSeller = room.getSeller().getId().equals(userId);
        boolean isBuyer = room.getBuyer().getId().equals(userId);

        if (!isSeller && !isBuyer) {
            throw new IllegalArgumentException("해당 채팅방의 참여자가 아닙니다.");
        }

        // 중복 완료 방지
        if (isSeller && room.getSellerStatus() == ChatStatus.COMPLETED) {
            throw new IllegalArgumentException("판매자 > 이미 완료.");
        }
        if (isBuyer && room.getBuyerStatus() == ChatStatus.COMPLETED) {
            throw new IllegalArgumentException("구매자 > 이미 완료.");
        }

        // 상태 업데이트
        if (isSeller) {
            room.setSellerStatus(ChatStatus.COMPLETED);
        } else {
            room.setBuyerStatus(ChatStatus.COMPLETED);
        }

        // 모두 완료 > chatroom finishdate 설정
        if (room.getSellerStatus() == ChatStatus.COMPLETED && room.getBuyerStatus() == ChatStatus.COMPLETED) {
            if (room.getFinishDate() == null) {
                room.setFinishDate(LocalDateTime.now());
            }
        }

        chatRoomRepository.save(room);

        return ChatResponseDTO.ChatCompleteRsDTO.builder()
                .buyerStatus(room.getBuyerStatus())
                .sellerStatus(room.getSellerStatus())
                .build();

    }

    //메시지 대화 내역 조회
    public ChatResponseDTO.GetMessageTotalListDTO getMessageTotalList(Long chatRoomId, Long userId){
        ChatRoom room = getChatRoomOrThrow(chatRoomId);

        boolean isSeller = room.getSeller().getId().equals(userId);
        boolean isBuyer = room.getBuyer().getId().equals(userId);

        if (!isSeller && !isBuyer) {
            throw new IllegalArgumentException("해당 채팅방의 참여자가 아닙니다.");
        }
        //채팅방의 모든 메시지 조회
        List<ChatMessage> messageList = chatMessageRepository.findByChatRoomId(chatRoomId);

        return ChatConverter.toDtoList(messageList, room.getId());
    }

    //채팅방 목록 조회
    public ChatResponseDTO.ChatRoomListResponseDTO getChatRoomList(Long userId, String type) {
        List<ChatRoom> chatRooms;

        //사용자 속한 채팅방(전체, 구매, 판매)
        switch (type.toLowerCase()) {
            case "buy" -> chatRooms = chatRoomRepository.findByBuyerId(userId);
            case "sell" -> chatRooms = chatRoomRepository.findBySellerId(userId);
            default -> chatRooms = chatRoomRepository.findBySellerIdOrBuyerId(userId, userId);
        }

        List<ChatResponseDTO.ChatRoomPreviewDTO> dtoList = chatRooms.stream()
                .map(room -> {
                    //채팅방 별로 제일 최근 메시지 찾아
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByCreatedAtDesc(room.getId());

                    //상대방
                    User partner = room.getSeller().getId().equals(userId)
                            ? room.getBuyer()
                            : room.getSeller();

                    //post 이미지 조회 > 없으면 null
                    PostImage postImageUrl = postImageRepository
                            .findFirstImageUrlByPostId(room.getPost().getId());
                    String imageUrl = postImageUrl != null ? postImageUrl.getImageUrl() : null;
                    return ChatConverter.toChatRoomPreviewDTO(room, lastMessage, partner, imageUrl);
                })
                .sorted((a, b) -> {
                    if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) {
                        // 둘 다 null이면 chatRoomId로 오름차순 정렬
                        return a.getChatRoomId().compareTo(b.getChatRoomId());
                    }
                    if (a.getLastMessageTime() == null) return 1;  // a가 아래로
                    if (b.getLastMessageTime() == null) return -1; // b가 아래로
                    return b.getLastMessageTime().compareTo(a.getLastMessageTime()); // 최신순 정렬
                })
                .collect(Collectors.toList());

        return new ChatResponseDTO.ChatRoomListResponseDTO(dtoList);


    }


}
