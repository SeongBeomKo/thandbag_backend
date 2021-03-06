package com.example.thandbag.controller;

import com.example.thandbag.TestConfig;
import com.example.thandbag.dto.login.LoginRequestDto;
import com.example.thandbag.dto.login.LoginResultDto;
import com.example.thandbag.dto.post.ThandbagRequestDto;
import com.example.thandbag.dto.post.ThandbagResponseDto;
import com.example.thandbag.dto.signup.SignupRequestDto;
import com.example.thandbag.model.Post;
import com.example.thandbag.model.User;
import com.example.thandbag.repository.LvImgRepository;
import com.example.thandbag.repository.PostRepository;
import com.example.thandbag.repository.ProfileImgRepository;
import com.example.thandbag.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LvImgRepository lvImgRepository;
    @Autowired
    private ProfileImgRepository profileImgRepository;

    private Long postId;

    private HttpHeaders headers;
    private final ObjectMapper mapper = new ObjectMapper();

    private String token = "";

    private SignupRequestDto user1 = SignupRequestDto.builder()
            .username("xxx@naver.com")
            .nickname("didl12")
            .password("tjds1234!")
            .mbti("INFJ")
            .build();

    private LoginRequestDto user1Login = LoginRequestDto.builder()
            .username("xxx@naver.com")
            .password("tjds1234!")
            .build();

    @AfterAll
    public void cleanup() {
        Optional<User> user = userRepository.findByUsername("xxx@naver.com");
        List<Post> postList = postRepository.findAllByUser(user.get());
        postRepository.deleteById(postId);
        userRepository.deleteById(user.get().getId());
        assertEquals(Optional.empty(),
                userRepository.findById(user.get().getId()));
        assertEquals(Optional.empty(),
                postRepository.findById(postList.get(0).getId()));
    }

    @BeforeAll
    public void preSet() {
        TestConfig.initialQuery(lvImgRepository, profileImgRepository);
    }

    @BeforeEach
    public void setup() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(1)
    @DisplayName("?????? ??????")
    void test1() throws JsonProcessingException {
        /* given */
        String requestBody = mapper.writeValueAsString(user1);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        /* when */
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/user/signup",
                request,
                String.class);

        /* then */
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("???????????? ??????", response.getBody());
    }

    @Test
    @Order(2)
    @DisplayName("?????????, JWT ?????? ??????")
    void test2() throws JsonProcessingException {
        /* given */
        String requestBody = mapper.writeValueAsString(user1Login);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        /* when */
        ResponseEntity<LoginResultDto> response = restTemplate.postForEntity(
                "/api/user/login",
                request,
                LoginResultDto.class);

        /* then */
        token = response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        assertNotEquals("", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Nested
    @DisplayName("????????? ????????? - main controller")
    class PostThandbag {
        @Test
        @Order(1)
        @DisplayName("????????? ????????? 1")
        void test1() throws JsonProcessingException {
            /* given */
            ThandbagRequestDto thandbagRequestDto = ThandbagRequestDto.builder()
                    .title("?????????")
                    .content("?????????")
                    .category("LOVE")
                    .share(true)
                    .build();

            String requestBody = mapper.writeValueAsString(thandbagRequestDto);
            headers.set("Authorization", token);
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            /* when */
            ResponseEntity<ThandbagResponseDto> response =
                    restTemplate.postForEntity(
                                "/api/newThandbag",
                                    request,
                                    ThandbagResponseDto.class);

            /* then */
            assertEquals(HttpStatus.OK, response.getStatusCode());
            Optional<User> user = userRepository.
                    findByUsername("xxx@naver.com");
            List<Post> postList = postRepository.findAllByUser(user.get());
            postId = postList.get(0).getId();
            System.out.println(postId);
            assertNotNull(postId);

        }

        @Test
        @Order(2)
        @DisplayName("????????? ????????????")
        void test2() throws JsonProcessingException {
            /* given */
            headers.set("Authorization", token);
            HttpEntity request = new HttpEntity(headers);

            /* when */
            ResponseEntity<Object> response = restTemplate.exchange(
                    "/api/thandbagList?page=0&size=2",
                        HttpMethod.GET,
                        request,
                        Object.class);

            /* then */
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @Order(3)
        @DisplayName("????????? ??????")
        void test3() throws JsonProcessingException {
            /* given */
            headers.set("Authorization", token);
            HttpEntity request = new HttpEntity(headers);

            /* when */
            ResponseEntity<Object> response = restTemplate.exchange(
                    "/api/thandbag?keyword=??????&page=0&size=2",
                        HttpMethod.GET,
                        request,
                        Object.class);
            List<ThandbagResponseDto> searchedThandbag =
                    (List<ThandbagResponseDto>) response.getBody();

            /* then */
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(searchedThandbag.size() >= 1);
        }

    }

}
