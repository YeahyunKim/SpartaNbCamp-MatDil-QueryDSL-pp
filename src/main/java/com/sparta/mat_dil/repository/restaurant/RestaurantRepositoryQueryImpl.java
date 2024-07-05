package com.sparta.mat_dil.repository.restaurant;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.mat_dil.entity.*;
import com.sparta.mat_dil.repository.restaurantLike.RestaurantLikeRepositoryQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RestaurantRepositoryQueryImpl implements RestaurantRepositoryQuery {
    private final JPAQueryFactory queryFactory;

    //팔로우 한 유저의 레스토랑 목록
    @Override
    public List<Restaurant> findFollowUserRestaurantsByUser(Long id, Pageable pageable) {
        QRestaurant qRestaurant = QRestaurant.restaurant;
        QFollow qFollow = QFollow.follow;

        JPAQuery<Restaurant> query = queryFactory.select(qRestaurant)
                .from(qRestaurant)
                .leftJoin(qFollow).on(qRestaurant.user.id.eq(qFollow.following.id))
                .where(qFollow.follower.id.eq(id))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<Restaurant> pathBuilder = new PathBuilder<Restaurant>(qRestaurant.getType(), qRestaurant.getMetadata());
            query.orderBy(new OrderSpecifier<>(
                    order.isAscending() ? com.querydsl.core.types.Order.ASC : Order.DESC,
                    pathBuilder.get(order.getProperty(), Comparable.class)
            ));
        }
        return query.fetch();
    }

    //팔로우 한 유저의 레스토랑 개수
    @Override
    public Long followUserRestaurantsCount(Long id){
        QRestaurant qRestaurant = QRestaurant.restaurant;
        QFollow qFollow = QFollow.follow;

        return queryFactory.select(qRestaurant.count())
                .from(qRestaurant)
                .leftJoin(qFollow).on(qRestaurant.user.id.eq(qFollow.following.id))
                .where(qFollow.follower.id.eq(id))
                .fetchOne();
    }
}