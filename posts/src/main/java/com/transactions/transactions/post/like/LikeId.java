package com.transactions.transactions.post.like;

import com.transactions.transactions.post.Post;

import java.io.Serializable;
import java.util.Objects;

public class LikeId implements Serializable {
    private Post post;
    private String userId;

    public LikeId() {}

    public LikeId(Post post, String userId) {
        this.post = post;
        this.userId = userId;
    }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeId likeId = (LikeId) o;
        return Objects.equals(post, likeId.post) &&
                Objects.equals(userId, likeId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(post, userId);
    }
}