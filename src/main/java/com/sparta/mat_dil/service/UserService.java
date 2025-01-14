package com.sparta.mat_dil.service;

import com.sparta.mat_dil.dto.*;
import com.sparta.mat_dil.entity.*;
import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.jwt.JwtUtil;
import com.sparta.mat_dil.repository.FollowRepository;
import com.sparta.mat_dil.repository.PasswordHistoryRepository;
import com.sparta.mat_dil.repository.restaurant.RestaurantRepository;
import com.sparta.mat_dil.repository.UserRepository;
import com.sparta.mat_dil.repository.commentLike.CommentLikeRepository;
import com.sparta.mat_dil.repository.restaurantLike.RestaurantLikeRepository;
import com.sparta.mat_dil.util.PageUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService extends PageUtil {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final RestaurantLikeRepository restaurantLikeRepository;
    private final RestaurantRepository restaurantRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    //회원가입
    @Transactional
    public void createUser(UserRequestDto requestDto) {
        //동일 아이디 검증
        validateUserId(requestDto.getAccountId());

        //동일 이메일 검증
        validateUserEmail(requestDto.getEmail());

        //비밀번호 암호화
        String password = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setPassword(password);
        userRepository.save(new User(requestDto));

    }

    //회원 탈퇴
    @Transactional
    public void withdrawUser(PasswordRequestDto requestDTO, User curruntUser) {

        User user = userRepository.findByAccountId(curruntUser.getAccountId()).orElse(null);
        if (user == null) {
            throw new CustomException(ErrorType.NOT_FOUND_USER);
        }
        //회원 상태 확인
        checkUserType(curruntUser.getUserStatus());

//        //비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDTO.getPassword(), curruntUser.getPassword())) {
            throw new CustomException(ErrorType.INVALID_PASSWORD);
        }

        //회원 상태 변경
        user.withdrawUser();
    }

    //동일 이메일 검증
    private void validateUserEmail(String email) {
        Optional<User> findUser = userRepository.findByEmail(email);

        if (findUser.isPresent()) {
            throw new CustomException(ErrorType.DUPLICATE_EMAIL);
        }
    }

    //동일 아이디 검증
    private void validateUserId(String id) {
        Optional<User> findUser = userRepository.findByAccountId(id);

        if (findUser.isPresent()) {
            throw new CustomException(ErrorType.DUPLICATE_ACCOUNT_ID);
        }
    }

    private void checkUserType(UserStatus userStatus) {
        if (userStatus.equals(UserStatus.DEACTIVATE)) {
            throw new CustomException(ErrorType.DEACTIVATE_USER);
        }
    }

    //회원 정보 수정
    @Transactional
    public ProfileResponseDto update(Long userId, ProfileRequestDto requestDto) {
        User user = findById(userId);
        String newEncodePassword = null;

        // 비밀번호 수정 시
        if (requestDto.getPassword() != null) {
            // 본인 확인을 위해 현재 비밀번호를 입력하여 올바른 경우
            if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                throw new CustomException(ErrorType.INVALID_PASSWORD);
            }
            //현재 비밀번호와 동일한 비밀번호로는 변경할 수 없음
            if (requestDto.getPassword().equals(requestDto.getNewPassword())) {
                throw new CustomException(ErrorType.PASSWORD_RECENTLY_USED);
            }
            // 최근 3번 안에 사용한 비밀번호는 사용할 수 없도록 제한
            List<PasswordHistory> recentPasswords = passwordHistoryRepository.findTop3ByUserOrderByChangeDateDesc(user);
            boolean isInPreviousPasswords = recentPasswords.stream()
                    .anyMatch(pw -> passwordEncoder.matches(requestDto.getNewPassword(), pw.getPassword()));
            if (isInPreviousPasswords) {
                throw new CustomException(ErrorType.PASSWORD_RECENTLY_USED);
            }

            newEncodePassword = passwordEncoder.encode(requestDto.getNewPassword());

            PasswordHistory passwordHistory = new PasswordHistory(user, newEncodePassword);
            passwordHistoryRepository.save(passwordHistory);
        }

        user.update(
                Optional.ofNullable(newEncodePassword),
                Optional.ofNullable(requestDto.getName()),
                Optional.ofNullable(requestDto.getIntro())
        );

        return new ProfileResponseDto(user);
    }

    @Transactional
    public void logout(User user, HttpServletResponse res, HttpServletRequest req) {
        user.logout();
        Cookie[] cookies = req.getCookies();
        String accessToken=jwtUtil.getAccessTokenFromRequest(req);
        jwtUtil.addBlackListToken(accessToken.substring(7));

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue(null);
                cookie.setPath("/");
                cookie.setMaxAge(0);

                res.addCookie(cookie);
            }
        }
    }

    @Transactional
    public ProfileResponseDto getProfile(Long userId) {
        ProfileResponseDto profileResponseDto = new ProfileResponseDto(findById(userId));
        long restaurantsLikedCnt = restaurantLikeRepository.countRestaurantLikesByUserId(userId);
        long commentsLikedCnt = commentLikeRepository.countCommentLikesByUserId(userId);
        profileResponseDto.updateContentLike(restaurantsLikedCnt, commentsLikedCnt);

        return profileResponseDto;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_USER)
        );
    }

    // 유저 팔로우
    @Transactional
    public void followUser(Long followingUserId, User follower) {
        // 1차 검증 팔로우할 유저 존재 여부
        User following = findById(followingUserId);

        // 2차 검증 자기자신 팔로우 불가
        if (followingUserId.equals(follower.getId())) {
            throw new CustomException(ErrorType.INVALID_FOLLOW_REQUEST);
        }

        System.out.println("follow 조회 = " + followRepository.findByFollowerAndFollowing(follower, following));

        // 3차 검증 중복 팔로우 불가능
        if (followRepository.findByFollowerAndFollowing(follower, following).isPresent()) {
            throw new CustomException(ErrorType.ALREADY_FOLLOWING);
        }

        Follow follow = new Follow(follower, following);

        followRepository.save(follow);
    }

    // 유저 언팔로우
    @Transactional
    public void unfollowUser(Long followingUserId, User follower) {
        // 1차 검증 언팔로우할 유저 존재 여부
        User following = findById(followingUserId);

        // 2차 검증 중복 언팔로우 불가능
        if (followRepository.findByFollowerAndFollowing(follower, following).isEmpty()) {
            throw new CustomException(ErrorType.NOT_FOUND_FOLLOW);
        }

        followRepository.delete(followRepository.findByFollowerAndFollowing(follower, following).get());
    }


    //팔로우 유저 레스토랑 목록 조회
    @Transactional(readOnly = true)
    public Page<RestaurantResponseDto> getFollowerRestaurants(User loginUser, int page, String sortBy) {
        Pageable pageable = createPageable(page, sortBy);

        List<Restaurant> restaurants = restaurantRepository.findFollowUserRestaurantsByUser(loginUser.getId(), pageable);

        Long pageCount = restaurantRepository.followUserRestaurantsCount(loginUser.getId());

        List<RestaurantResponseDto> restatrantRestaurantDtoList = restaurants.stream().map(RestaurantResponseDto::new).toList();

        return new PageImpl<>(restatrantRestaurantDtoList, pageable, pageCount);
    }
}
