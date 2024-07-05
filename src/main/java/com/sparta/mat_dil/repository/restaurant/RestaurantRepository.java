package com.sparta.mat_dil.repository.restaurant;

import com.sparta.mat_dil.entity.Restaurant;
import com.sparta.mat_dil.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantRepositoryQuery {
    Page<Restaurant> findAll(Pageable pageable);
}
