package com.sparta.mat_dil.repository.restaurantLike;

import com.sparta.mat_dil.entity.Restaurant;

import java.util.List;

public interface RestaurantLikeRepositoryQuery {
    List<Restaurant> findLikedRestaurantsByUser(Long id, int limit);

}