package com.example.thandbag.dto.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultDto {

    private Long userId;
    private String nickname;
    private int level;
    private String mbti;
    private String profileImgUrl;
}
