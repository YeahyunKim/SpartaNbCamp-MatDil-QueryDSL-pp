package com.sparta.mat_dil.controller;

import com.sparta.mat_dil.dto.LikeResponseDto;
import com.sparta.mat_dil.dto.ResponseDataDto;
import com.sparta.mat_dil.enums.ContentTypeEnum;
import com.sparta.mat_dil.enums.ResponseStatus;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.security.UserDetailsImpl;
import com.sparta.mat_dil.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/{contentType}/{contentId}")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PutMapping("/like")
    public ResponseEntity<ResponseDataDto<LikeResponseDto>> updateRestaurantLike(@PathVariable("contentType") ContentTypeEnum contentType, @PathVariable("contentId") Long contentId, @AuthenticationPrincipal UserDetailsImpl userDetails) throws CustomException {

        LikeResponseDto likeResponseDto = likeService.updateContentLike(contentType, contentId, userDetails.getUser());

        ResponseStatus responseStatus = likeResponseDto.isLiked() ? ResponseStatus.LIKE_CREATE_SUCCESS : ResponseStatus.LIKE_DELETE_SUCCESS;

        return ResponseEntity.ok(new ResponseDataDto<>(responseStatus, likeResponseDto));
    }
}
