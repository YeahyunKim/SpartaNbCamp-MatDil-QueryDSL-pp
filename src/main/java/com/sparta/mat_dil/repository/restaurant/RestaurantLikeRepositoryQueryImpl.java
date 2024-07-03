package com.sparta.mat_dil.repository.restaurant;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.mat_dil.entity.QRestaurantLike;
import com.sparta.mat_dil.entity.RestaurantLike;
import com.sparta.mat_dil.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RestaurantLikeRepositoryImplQuery implements RestaurantLikeRepositoryQuery {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<RestaurantLike> findLikedRestaurantsByUser(User user, int offset, int limit) {
        QRestaurantLike qRestaurantLike = QRestaurantLike.restaurantLike;

        return queryFactory.selectFrom(qRestaurantLike)
                .where(qRestaurantLike.user.eq(user))
                .orderBy(qRestaurantLike.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}