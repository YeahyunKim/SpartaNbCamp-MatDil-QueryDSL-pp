package com.sparta.mat_dil.service;

import com.sparta.mat_dil.dto.LikeResponseDto;
import com.sparta.mat_dil.entity.*;
import com.sparta.mat_dil.enums.ContentTypeEnum;
import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final UserRepository userRepository;
    private final RestaurantLikeRepository restaurantLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final RestaurantRepository restaurantRepository;

    public LikeResponseDto updateContentLike(ContentTypeEnum contentType, Long contentId, User loginUser) {
        switch (contentType) {
            case RESTAURANT:
                return updateRestaurantLike(contentId, loginUser);
            case COMMENT:
                return updateCommentLike(contentId, loginUser);
            default:
                throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }
    }

    /** [updateRestaurantLike()] 음식점 좋아요 업데이트
    * @param contentId 컨텐트 아이디
    * @param loginUser 로그인 유저 정보
    * @return LikeResponseDto
    **/
    private LikeResponseDto updateRestaurantLike(Long contentId, User loginUser) {

        validateUser(loginUser);

        Restaurant restaurant = getValidatedRestaurant(contentId);

        if (loginUser.getAccountId().equals(restaurant.getUser().getAccountId())) {
            throw new CustomException(ErrorType.CONTENT_OWNER);
        }

        RestaurantLike restaurantLike = restaurantLikeRepository.findByUserAndRestaurant(loginUser, restaurant)
                .orElseGet(() -> new RestaurantLike(loginUser, restaurant));

        restaurantLike.updateLiked();

        restaurantLikeRepository.save(restaurantLike);

        return calculateRestaurantLike(restaurantLike, restaurant);
    }

    /** [updateCommentLike()] 댓글 좋아요 업데이트
    * @param contentId 컨텐트 아이디
    * @param loginUser 로그인 유저 정보
    * @return LikeResponseDto
    **/
    private LikeResponseDto updateCommentLike(Long contentId, User loginUser) {
        validateUser(loginUser);

        Comment comment = getValidatedComment(contentId);

        if (loginUser.getAccountId().equals(comment.getUser().getAccountId())) {
            throw new CustomException(ErrorType.CONTENT_OWNER);
        }

        CommentLike commentLike = commentLikeRepository.findByUserAndComment(loginUser, comment)
                .orElseGet(() -> new CommentLike(loginUser, comment));

        commentLike.updateLiked();
        commentLikeRepository.save(commentLike);

        return calculateCommentLike(commentLike, comment);
    }


    //유저 검증 로직
    public void validateUser(User loginUser){
        userRepository.findById(loginUser.getId()).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_USER));

        if(loginUser.getUserStatus().equals(UserStatus.DEACTIVATE)){
            throw new CustomException(ErrorType.DEACTIVATE_USER);
        }

        if(loginUser.getUserStatus().equals(UserStatus.BLOCKED)){
            throw new CustomException(ErrorType.BLOCKED_USER);
        }
    }

    //레스토랑 검증 및 정보 가져오기
    public Restaurant getValidatedRestaurant(Long restaurantId){
        return restaurantRepository.findById(restaurantId).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_RESTAURANT));
    }

    //댓글 검증 및 정보 가져오기
    public Comment getValidatedComment(Long commentId){
        return commentRepository.findById(commentId).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_COMMENT));
    }

    private LikeResponseDto calculateRestaurantLike(RestaurantLike restaurantLike, Restaurant restaurant) {
        Long cnt =  restaurant.updateLike(restaurantLike.isLiked());
        return new LikeResponseDto(restaurantLike.isLiked(), cnt);
    }

    public LikeResponseDto calculateCommentLike(CommentLike commentLike, Comment comment) {
        Long cnt =  comment.updateLike(commentLike.isLiked());
        return new LikeResponseDto(commentLike.isLiked(), cnt);
    }
}
