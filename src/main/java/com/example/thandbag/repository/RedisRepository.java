package com.example.thandbag.repository;

import com.example.thandbag.dto.chat.chatroom.ChatRoomDto;
import com.example.thandbag.dto.chat.chatroom.CreateRoomRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RedisRepository {

    /* Redis CacheKeys */
    private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
    public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
    public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장
    public static final String USER_NICKNAME = "USER_NICKNAME"; // 채팅룸에 입장한 유저 닉네임

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoomDto> hashOpsChatRoom;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoomDto> hashOpsChatList;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;

    /* 모든 채팅방 조회 */
    public List<ChatRoomDto> findAllRoom() {
        return hashOpsChatRoom.values(CHAT_ROOMS);
    }

    /* 특정 채팅방 조회 */
    public ChatRoomDto findRoomById(String id) {
        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    /* 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다. */
    public ChatRoomDto createChatRoom(CreateRoomRequestDto createRoomRequestDto) {
        ChatRoomDto chatRoomDto = new ChatRoomDto(createRoomRequestDto);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoomDto.getRoomId(), chatRoomDto);
        return chatRoomDto;
    }

    /* 유저가 입장한 채팅방ID와 유저 세션ID 맵핑 정보 저장 */
    public void setUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }

    /* 유저 세션으로 입장해 있는 채팅방 ID 조회 */
    public String getUserEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    /* 유저 세션정보와 맵핑된 채팅방ID 삭제 */
    public void removeUserEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }

    /* 세션에 유저 닉네임 저장 */
    public void setNickname(String sessionId, String nickname) {
        hashOpsEnterInfo.put(USER_NICKNAME, sessionId, nickname);
    }

    /* 세션에서 유저 닉네임 로드 */
    public String getNickname(String sessionId) {
        return hashOpsEnterInfo.get(USER_NICKNAME, sessionId);
    }

    /* 채팅방 유저수 조회 */
    public long getUserCount(String roomId) {
        return Long.valueOf(
                Optional.ofNullable(valueOps.get(USER_COUNT + "_" + roomId))
                        .orElse("0"));
    }

    /* 채팅방에 입장한 유저수 +1 */
    public long plusUserCount(String roomId) {
        return Optional.ofNullable(valueOps.increment(USER_COUNT + "_" + roomId))
                .orElse(0L);
    }

    /* 채팅방에 입장한 유저수 -1 */
    public long minusUserCount(String roomId) {
        return Optional.ofNullable(valueOps.decrement(USER_COUNT + "_" + roomId))
                .filter(count -> count > 0)
                .orElse(0L);
    }

    /* refreshToken  저장 */
    public void saveRefreshToken(String token, String username) {
        System.out.println("redis repo 에 refresh token 저장:" + token);
        valueOps.set(username, token, Duration.ofHours(1L));
    }

    /* refreshToken  저장 */
    public boolean checkRefreshToken(String token, String username) {
        return valueOps.get(username) != null ?
                (valueOps.get(username).equals(token) ? true : false) : false;
    }

    /* refreshToken 삭제 */
    public void deleteRefreshToken(String username) {
        System.out.println("refresh token 삭제 완료");
        valueOps.getAndDelete(username);
    }
}