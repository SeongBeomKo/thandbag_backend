package com.example.thandbag.controller;

import com.example.thandbag.dto.ThandbagRequestDto;
import com.example.thandbag.dto.ThandbagResponseDto;
import com.example.thandbag.security.UserDetailsImpl;
import com.example.thandbag.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    // 생드백 만들기
    @CrossOrigin("*")
    @PostMapping("/api/newThandbag")
    public ThandbagResponseDto createThandbag(@RequestBody ThandbagRequestDto thandbagRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // @AuthenticationPrincipal UserDetailsImpl userDetails
        return mainService.createThandbag(thandbagRequestDto, userDetails);
    }

    // 공개된 생드백 전체 리스트 보기
    @CrossOrigin("*")
    @GetMapping("/api/thandbagList")
    public List<ThandbagResponseDto> allSharedThandbag(@RequestParam int page, @RequestParam int size) {
        return mainService.showAllThandbag(page, size);
    }

    // 검색된 생드백 보기
    @CrossOrigin("*")
    @GetMapping("/api/thandbag")
    public List<ThandbagResponseDto> searchThandbags(@RequestParam String keyword, @RequestParam int page, @RequestParam int size) {
        return mainService.searchThandbags(keyword, page, size);
    }
}
