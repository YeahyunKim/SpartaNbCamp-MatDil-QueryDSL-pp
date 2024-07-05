package com.sparta.mat_dil.repository.restaurant;

import com.sparta.mat_dil.entity.Restaurant;
import com.sparta.mat_dil.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestaurantRepositoryQuery {
    List<Restaurant> findFollowUserRestaurantsByUser(Long id, Pageable pageable);
    Long followUserRestaurantsCount(Long id);
}