package com.example.thandbag.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThandbagRequestDto {

    private String title;
    private String content;
    private List<MultipartFile> imgUrl;
    private String category;
    private boolean share;
}
