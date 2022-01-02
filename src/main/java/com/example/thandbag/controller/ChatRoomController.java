package com.example.thandbag.controller;

import com.example.thandbag.dto.ChatHistoryResponseDto;
import com.example.thandbag.dto.ChatMyRoomListResponseDto;
import com.example.thandbag.dto.ChatRoomDto;
import com.example.thandbag.dto.CreateRoomRequestDto;
import com.example.thandbag.model.LoginInfo;
import com.example.thandbag.repository.ChatRedisRepository;
import com.example.thandbag.security.UserDetailsImpl;
import com.example.thandbag.security.jwt.JwtTokenUtils;
import com.example.thandbag.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatRoomController {
    private final ChatRedisRepository chatRedisRepository;
    private final ChatService chatService;

    @CrossOrigin(exposedHeaders = "Authorization", originPatterns = "*")
    @GetMapping("/chat/user")
    public LoginInfo getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
        String name = auth.getName();
        return LoginInfo.builder().name(name).token(JwtTokenUtils.generateJwtToken(user)).build();
    }

    // 내가 참가한 모든 채팅방 목록
    @CrossOrigin(exposedHeaders = "Authorization", originPatterns = "*")
    @GetMapping("/chat/myRoomList")
    public List<ChatMyRoomListResponseDto> room(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.findMyChatList(userDetails.getUser());
    }

    // 채팅방 생성
    @CrossOrigin(exposedHeaders = "Authorization", originPatterns = "*")
    @PostMapping("/chat/room")
    public ChatRoomDto createRoom(@RequestBody CreateRoomRequestDto roomRequestDto) {
        return chatService.createChatRoom(roomRequestDto);
    }

    // 채팅방 입장 화면
    @CrossOrigin(exposedHeaders = "Authorization", originPatterns = "*")
    @PostMapping("/chat/room/enter/{roomId}")
    public List<ChatHistoryResponseDto> roomDetail(@PathVariable String roomId) {
        return chatService.getTotalChatContents(roomId);
    }

    // 특정 채팅방 조회
    @CrossOrigin(exposedHeaders = "Authorization", originPatterns = "*")
    @PostMapping("/chat/room/{roomId}")
    public ChatRoomDto roomInfo(@PathVariable String roomId) {
        return chatRedisRepository.findRoomById(roomId);
    }
}