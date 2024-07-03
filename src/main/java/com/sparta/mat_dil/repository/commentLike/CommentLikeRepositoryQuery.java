package com.sparta.mat_dil.repository.commentLike;

import com.sparta.mat_dil.entity.Comment;

import java.util.List;

public interface CommentLikeRepositoryQuery {
    List<Comment> findLikedCommentsByUser(Long id, int limit);

}
