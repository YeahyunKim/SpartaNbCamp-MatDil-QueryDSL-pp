package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.RestaurantLike;
import com.sparta.mat_dil.entity.User;

import java.util.List;

public interface RestaurantLikeQueryRepository {
    List<RestaurantLike> findLikedRestaurantsByUser(User user, int offset, int limit);
}