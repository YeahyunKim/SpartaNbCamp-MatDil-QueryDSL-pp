package com.sparta.mat_dil.service;

import com.sparta.mat_dil.dto.CommentResponseDto;
import com.sparta.mat_dil.dto.LikeResponseDto;
import com.sparta.mat_dil.dto.RestaurantResponseDto;
import com.sparta.mat_dil.entity.*;
import com.sparta.mat_dil.enums.ContentTypeEnum;
import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.repository.*;
import com.sparta.mat_dil.repository.commentLike.CommentLikeRepository;
import com.sparta.mat_dil.repository.CommentRepository;
import com.sparta.mat_dil.repository.restaurantLike.RestaurantLikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    /** [getLikedRestaurants()] 좋아요 누른 레스토랑 리스트 가져오기
     * @param loginUser 로그인 유저 정보
     * @param page 요청할 페이지 번호
     * @param size 안에 컨텐트 수
    * @return List<RestaurantResponseDto>
    **/
    public Page<RestaurantResponseDto> getLikedRestaurants(User loginUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int limit = pageable.getPageSize();

        List<Restaurant> likedRestaurants = restaurantLikeRepository.findLikedRestaurantsByUser(loginUser.getId(), limit);

        return new PageImpl<>(likedRestaurants.stream()
                .map(RestaurantResponseDto::new)
                .collect(Collectors.toList()));
    }

    /** [getLikedComments()] 좋아요 누른 댓글 리스트 가져오기
     * @param loginUser 로그인 유저 정보
     * @param page 요청할 페이지 번호
     * @param size 안에 컨텐트 수
     * @return List<RestaurantResponseDto>
     **/
    public Page<CommentResponseDto> getLikedComments(User loginUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int limit = pageable.getPageSize();

        List<Comment> likedComments = commentLikeRepository.findLikedCommentsByUser(loginUser.getId(), limit);

        return new PageImpl<>(likedComments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList()));
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
