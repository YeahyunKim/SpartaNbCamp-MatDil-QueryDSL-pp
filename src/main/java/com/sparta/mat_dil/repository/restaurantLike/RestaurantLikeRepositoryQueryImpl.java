package com.sparta.mat_dil.repository.restaurantLike;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.mat_dil.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RestaurantLikeRepositoryQueryImpl implements RestaurantLikeRepositoryQuery {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Restaurant> findLikedRestaurantsByUser(Long id, int limit) {
        QRestaurant qRestaurant = QRestaurant.restaurant;
        QRestaurantLike qRestaurantLike = QRestaurantLike.restaurantLike;

        return queryFactory.select(qRestaurant)
                .from(qRestaurant)
                .join(qRestaurantLike).on(qRestaurant.id.eq(qRestaurantLike.restaurant.id))
                .where(qRestaurantLike.user.id.eq(id).and(qRestaurantLike.Liked.isTrue()))
                .orderBy(qRestaurantLike.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}