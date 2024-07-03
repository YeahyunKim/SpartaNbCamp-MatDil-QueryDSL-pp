package com.sparta.mat_dil.repository.restaurant;

import com.sparta.mat_dil.entity.Restaurant;
import com.sparta.mat_dil.entity.RestaurantLike;
import com.sparta.mat_dil.entity.User;

import java.util.List;

public interface RestaurantLikeRepositoryQuery {
    List<Restaurant> findLikedRestaurantsByUser(Long id, int limit);

}