package com.sparta.mat_dil.dto;

import com.sparta.mat_dil.entity.Restaurant;
import lombok.Getter;

@Getter
public class RestaurantResponseDto {
    private long restaurantId;
    private long userId;
    private String userName;
    private String restaurantName;
    private String description;
    private long likesCnt;

    public RestaurantResponseDto(Restaurant restaurant) {
        this.restaurantId = restaurant.getId();
        this.userId = restaurant.getUser().getId();
        this.userName = restaurant.getUser().getName();
        this.restaurantName = restaurant.getRestaurantName();
        this.description = restaurant.getDescription();
        this.likesCnt = restaurant.getLikesCnt();
    }

}