package com.posts.post.post.reply;

import com.posts.post.post.comment.CreateCommentDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyDto extends CreateCommentDto {
    @NotNull
    private Long parentId;
}