package com.example.thandbag.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.thandbag.Enum.Action;
import com.example.thandbag.Enum.Category;
import com.example.thandbag.dto.post.ThandbagRequestDto;
import com.example.thandbag.dto.post.ThandbagResponseDto;
import com.example.thandbag.model.Post;
import com.example.thandbag.model.PostImg;
import com.example.thandbag.model.User;
import com.example.thandbag.repository.*;
import com.example.thandbag.security.UserDetailsImpl;
import com.example.thandbag.security.jwt.JwtDecoder;
import com.example.thandbag.security.jwt.JwtTokenUtils;
import com.example.thandbag.timeconversion.TimeConversion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.thandbag.security.jwt.JwtTokenUtils.CLAIM_EXPIRED_DATE;

@RequiredArgsConstructor
@Service
public class MainService {

    private final AlarmService alarmService;
    private final PostRepository postRepository;
    private final PostImgRepository postImgRepository;
    private final UserRepository userRepository;
    private final LvImgRepository lvImgRepository;
    private final ImageService imageService;
    private final AlarmRepository alarmRepository;
    private final RedisTemplate redisTemplate;
    private final ChannelTopic channelTopic;
    private final RedisRepository redisRepository;
    private final JwtDecoder jwtDecoder;
    private final JwtTokenUtils jwtTokenUtils;

    /* ????????? ?????? */
    @Transactional
    public ThandbagResponseDto createThandbag(
            ThandbagRequestDto thandbagRequestDto, User user,
            HttpServletResponse response, HttpServletRequest request)
            throws IOException {

        /* token ?????? */
        DecodedJWT decodedJWT = jwtDecoder
                .isValidToken(request.getHeader("Authorization").substring(7))
                .orElseThrow(() -> new IllegalArgumentException(
                        "?????? ????????? ???????????? ????????????."));

        Date expiredDate = decodedJWT
                .getClaim(CLAIM_EXPIRED_DATE)
                .asDate();

        if (expiredDate.before(new Date())) {
            if (!redisRepository
                    .checkRefreshToken(request.getHeader("refreshToken").substring(7),
                            user.getUsername())) {
                UserDetailsImpl userDetails = new UserDetailsImpl(user);
                String accessToken = jwtTokenUtils.generateJwtToken(userDetails, JwtTokenUtils.SEC * 10);
                response.setHeader("Authorization", accessToken);
            }
        }

        /* ?????? ????????? ???????????? */
        List<PostImg> postImgList = new ArrayList<>();
        List<String> fileUrlList = new ArrayList<>();

        /* ????????? ???????????? ?????? ???????????? */
        if (thandbagRequestDto.getImgUrl() != null) {
            for (MultipartFile multipartFile : thandbagRequestDto.getImgUrl()) {
                String imgUrl = imageService.uploadFile(multipartFile);
                PostImg img = new PostImg(imgUrl);
                fileUrlList.add(imgUrl);
                postImgList.add(img);
            }
        }

        /* ????????? ?????? */
        Post post = Post.builder()
                .title(thandbagRequestDto.getTitle())
                .category(Category.valueOf(thandbagRequestDto.getCategory()))
                .closed(false)
                .content(thandbagRequestDto.getContent())
                .imgList(postImgList)
                .share(thandbagRequestDto.isShare())
                .user(user)
                .commentCount(0)
                .build();
        post = postRepository.save(post);

        /* ?????? ????????? ??? count */
        user.plusTotalPostsAndComments();

        /* ????????? ?????? ?????? */
        alarmService.generateLevelAlarm(user, Action.POST);

        user = userRepository.save(user);
        System.out.println("id: " + post.getId());
        Post posted = postRepository.findById(post.getId()).orElseThrow(
                () -> new NullPointerException("post??? ????????????"));

        /* ???????????? lv */
        System.out.println("level: " + user.getLevel());
        String bannerLv = lvImgRepository.findByTitleAndLevel(
                "???????????? ??????",
                user.getLevel()
        ).getLvImgUrl();

        return ThandbagResponseDto.builder()
                .userId(user.getId())
                .category(thandbagRequestDto.getCategory())
                .createdAt(TimeConversion.timeConversion(posted.getCreatedAt()))
                .share(thandbagRequestDto.isShare())
                .imgUrl(fileUrlList)
                .totalCount(user.getTotalCount())
                .content(thandbagRequestDto.getContent())
                .nickname(user.getNickname())
                .level(user.getLevel())
                .mbti(user.getMbti())
                .lvImg(bannerLv)
                .closed(posted.getClosed())
                .share(posted.getShare())
                .hitCount(posted.getTotalHitCount())
                .totalCount(user.getTotalCount())
                .title(thandbagRequestDto.getTitle())
                .build();
    }

    /* ????????? ?????? ????????? ???????????? ????????? */
    public List<ThandbagResponseDto> showAllThandbag(int page, int size) {
        Pageable pageable =
                PageRequest.of(page, size, Sort.by("createdAt")
                        .descending());

        List<ThandbagResponseDto> allThandbags = new ArrayList<>();

        List<Post> allPosts = postRepository
                .findAllByShareTrueOrderByCreatedAtDesc(pageable).getContent();
        //.findAllByShareTrueOrderByCreatedAtDesc();

        for (Post post : allPosts) {
            ThandbagResponseDto thandbagResponseDto =
                    createThandbagResponseDto(post);
            allThandbags.add(thandbagResponseDto);
        }
        return allThandbags;
    }

    /* ????????? ????????? ?????? ????????? ???????????? ????????? */
    public List<ThandbagResponseDto> searchThandbags(String keyword,
                                                     int pageNumber,
                                                     int size) {
        Pageable sortedByModifiedAtDesc = PageRequest.of(pageNumber,
                size,
                Sort.by("modifiedAt").descending()
        );

        List<Post> posts = postRepository
                .findAllByShareTrueAndContainsKeywordForSearch(keyword,
                        sortedByModifiedAtDesc).getContent();

        /* dto ?????? */
        List<ThandbagResponseDto> searchedPosts = new ArrayList<>();
        for (Post post : posts) {
            ThandbagResponseDto thandbagResponseDto =
                    createThandbagResponseDto(post);
            searchedPosts.add(thandbagResponseDto);
        }

        return searchedPosts;
    }

    /* ????????? ????????? ?????? ????????? ?????? ???????????? dto ????????? ?????? */
    public ThandbagResponseDto createThandbagResponseDto(Post post) {

        return ThandbagResponseDto.builder()
                .postId(post.getId())
                .userId(post.getUser().getId())
                .nickname(post.getUser().getNickname())
                /* ispresent check ????????? */
                .level(post.getUser().getLevel())
                .title(post.getTitle())
                .category(post.getCategory().getCategory())
                .createdAt(TimeConversion.timeConversion(post.getCreatedAt()))
                .closed(post.getClosed())
                .mbti(post.getUser().getMbti())
                .commentCount(post.getCommentCount())
                .totalCount(post.getUser().getTotalCount())
                .hitCount(post.getTotalHitCount())
                .content(post.getContent())
//                .profileImgUrl(post.getUser()
//                        .getProfileImg()
//                        .getProfileImgUrl()
//                )
                .share(post.getShare())
                .closed(post.getClosed())
                .build();
    }
}
