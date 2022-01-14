package com.example.thandbag.service;

import com.example.thandbag.Enum.Auth;
import com.example.thandbag.Enum.Category;
import com.example.thandbag.dto.post.BestUserDto;
import com.example.thandbag.dto.post.HitCountDto;
import com.example.thandbag.dto.post.ThandbagRequestDto;
import com.example.thandbag.dto.post.ThandbagResponseDto;
import com.example.thandbag.model.*;
import com.example.thandbag.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThandbagDetailServiceTest {

    @Mock
    PostRepository postRepository;
    @Mock
    LvImgRepository lvImgRepository;
    @Mock
    CommentLikeRepository commentLikeRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    AlarmRepository alarmRepository;
    @Mock
    RedisTemplate redisTemplate;
    @Mock
    ChannelTopic channelTopic;

    ThandbagDetailService thandbagDetailService;

    private Long id;
    private String title;
    private String content;
    private Boolean closed;
    private Boolean share;
    private User user;
    private Category category;
    private List<Comment> commentList;
    private MainService mainService;
    private ThandbagRequestDto thandbagRequestDto;
    private Post post;
    private List<PostImg> postImgList;
    private Comment comment;

    @BeforeEach
    void setup() {
        title = "노잼";
        content = "레알 테케 쓰는거 개노잼";
        closed = false;
        share = true;
        user = User.builder()
                .id(1L)
                .username("asb@abc.abc")
                .password("1234")
                .nickname("nojam")
                .mbti("INTP")
                .totalCount(1)
                .level(1)
                .profileImg(new ProfileImg(1L, "asdf"))
                .auth(Auth.USER).build();
        category = Category.LOVE;
        thandbagDetailService = new ThandbagDetailService(postRepository,
                lvImgRepository,
                commentLikeRepository,
                alarmRepository,
                redisTemplate,
                channelTopic);
        commentList = new ArrayList<>();
        thandbagRequestDto = new ThandbagRequestDto(title, content, null, "LOVE", share);
        postImgList = new ArrayList<>();

        post = Post.builder()
                .id(1L)
                .title(thandbagRequestDto.getTitle())
                .category(category)
                .closed(thandbagRequestDto.isShare())
                .content(thandbagRequestDto.getContent())
                .imgList(postImgList)
                .share(thandbagRequestDto.isShare())
                .user(user)
                .commentList(commentList)
                .totalHitCount(0)
                .build();
        post.setCreatedAt(LocalDateTime.now());

        comment = Comment.builder()
                .comment(content)
                .likedByWriter(false)
                .user(user)
                .post(post)
                .id(1L)
                .build();
    }


    @DisplayName("샌드백 상세보기")
    @Test
    @Order(1)
    void getOneThandbag() {

        post.getCommentList().add(comment);
        comment.setCreatedAt(LocalDateTime.now());

        //given
        given(postRepository.findById(post.getId()))
                .willReturn(Optional.of(post));
        given(lvImgRepository.findByTitleAndLevel(anyString(),anyInt()))
                .willReturn(new LvImg("asdf", "asdf", 1));

        //when
        thandbagDetailService = new ThandbagDetailService(postRepository, lvImgRepository, commentLikeRepository, alarmRepository, redisTemplate, channelTopic);
        ThandbagResponseDto thandbagResponseDto = thandbagDetailService.getOneThandbag(post.getId(), user);

        //then
        assertNotNull(thandbagResponseDto);
        assertEquals(thandbagResponseDto.getUserId(), 1);
        assertEquals(1, thandbagResponseDto.getComments().size());
        assertEquals("노잼", thandbagResponseDto.getTitle());

    }

    @DisplayName("샌드백 삭제")
    @Test
    @Order(2)
    void removeThandbag() {

        //when
        thandbagDetailService.removeThandbag(1, user);

        //then
        assertEquals(0, user.getTotalCount());
    }

    @DisplayName("샌드백 떠트리기")
    @Test
    @Order(3)
    void completeThandbag() {

        post.getCommentList().add(comment);
        comment.setCreatedAt(LocalDateTime.now());


        //given
        HitCountDto hitCountDto = new HitCountDto(0, 10);

        User user1 = User.builder()
                .id(2L)
                .username("hh@hoho.haha")
                .password("1234")
                .nickname("뀨류잼")
                .mbti("ESFJ")
                .totalCount(1)
                .level(1)
                .profileImg(new ProfileImg(2L, "asdf"))
                .auth(Auth.USER).build();

        Comment comment2 = Comment.builder()
                .comment("테케 레알 꿀잼")
                .likedByWriter(true)
                .user(user1)
                .post(post)
                .id(2L)
                .build();

        post.getCommentList().add(comment2);
        comment2.setCreatedAt(LocalDateTime.now());

        given(postRepository.findById(post.getId()))
                .willReturn(Optional.of(post));

        given(channelTopic.getTopic())
                .willReturn("aside");

        //when
        thandbagDetailService = new ThandbagDetailService(postRepository, lvImgRepository, commentLikeRepository, alarmRepository, redisTemplate, channelTopic);
        List<BestUserDto> bestUserDtoList = thandbagDetailService.completeThandbag(post.getId(),hitCountDto);

        //then

        assertEquals(1, bestUserDtoList.size());
        assertEquals("뀨류잼", bestUserDtoList.get(0).getNickname());
        assertEquals("ESFJ", bestUserDtoList.get(0).getMbti());
    }
}