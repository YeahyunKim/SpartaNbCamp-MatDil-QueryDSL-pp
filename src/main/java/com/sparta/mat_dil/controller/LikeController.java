package com.sparta.mat_dil.controller;

import com.sparta.mat_dil.dto.CommentResponseDto;
import com.sparta.mat_dil.dto.LikeResponseDto;
import com.sparta.mat_dil.dto.ResponseDataDto;
import com.sparta.mat_dil.dto.RestaurantResponseDto;
import com.sparta.mat_dil.entity.User;
import com.sparta.mat_dil.enums.ContentTypeEnum;
import com.sparta.mat_dil.enums.ResponseStatus;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.security.UserDetailsImpl;
import com.sparta.mat_dil.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PutMapping("/{contentType}/{contentId}/like")
    public ResponseEntity<ResponseDataDto<LikeResponseDto>> updateRestaurantLike(@PathVariable("contentType") ContentTypeEnum contentType, @PathVariable("contentId") Long contentId, @AuthenticationPrincipal UserDetailsImpl userDetails) throws CustomException {

        LikeResponseDto likeResponseDto = likeService.updateContentLike(contentType, contentId, userDetails.getUser());

        ResponseStatus responseStatus = likeResponseDto.isLiked() ? ResponseStatus.LIKE_CREATE_SUCCESS : ResponseStatus.LIKE_DELETE_SUCCESS;

        return ResponseEntity.ok(new ResponseDataDto<>(responseStatus, likeResponseDto));
    }

    @GetMapping("/RESTAURANT/liked")
    public Page<RestaurantResponseDto> getLikedRestaurants(
            @AuthenticationPrincipal UserDetailsImpl loginUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return likeService.getLikedRestaurants(loginUser.getUser(), page, size);
    }

    @GetMapping("/COMMENT/liked")
    public Page<CommentResponseDto> getLikedComments(
            @AuthenticationPrincipal UserDetailsImpl loginUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return likeService.getLikedComments(loginUser.getUser(), page, size);
    }
}
