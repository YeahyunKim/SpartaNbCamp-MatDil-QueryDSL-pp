package com.sparta.mat_dil.repository.commentLike;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.mat_dil.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentLikeRepositoryQueryImpl implements CommentLikeRepositoryQuery{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findLikedCommentsByUser(Long id, int limit) {
        QComment qComment = QComment.comment;
        QCommentLike qCommentLike = QCommentLike.commentLike;

        return queryFactory.select(qComment)
                .from(qComment)
                .join(qCommentLike).on(qComment.id.eq(qCommentLike.comment.id))
                .where(qCommentLike.user.id.eq(id).and(qCommentLike.Liked.isTrue()))
                .orderBy(qCommentLike.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Long countCommentLikesByUserId(Long userId) {
        QCommentLike qCommentLike = QCommentLike.commentLike;
        return queryFactory.select(qCommentLike.count())
                .from(qCommentLike)
                .where(qCommentLike.user.id.eq(userId))
                .fetchOne();
    }
}
