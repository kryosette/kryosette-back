package com.posts.post.post;

import com.posts.post.post.like.LikeRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.attribute.UserPrincipalNotFoundException;

@RestController
@RequestMapping("posts")
@RequiredArgsConstructor
@Tag(name = "Posts")
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
//    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final AuthServiceClient authServiceClient;
//    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(
            @RequestBody @Valid PostCreateRequest request,
            @RequestHeader("Authorization") String authHeader
    ) throws UserPrincipalNotFoundException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");
        Post post = postService.createPost(request, token);
        return ResponseEntity.ok(post).getBody();
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int actualSize = (page == 0) ? 10 : 5;
        Pageable pageable = PageRequest.of(page, actualSize, Sort.by("createdAt").descending());

        Page<PostDto> postPage = postService.getAllPosts(pageable)
                .map(this::mapPostToDto);

        return ResponseEntity.ok(postPage);
    }

    @Transactional
    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createLike(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer postId
    ) throws UserPrincipalNotFoundException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");
        ResponseEntity<?> like = postService.createLike(token, postId);
        return ResponseEntity.ok(like).getBody();
    }

//    @Transactional
//    @PostMapping("/{postId}/like")
//    public ResponseEntity<?> likePost(
//            @PathVariable Integer postId,
//            @RequestHeader("Authorization") String authHeader,
//            String userId
//    ) {
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
//        }
//        String token = authHeader.replace("Bearer ", "");
//
//        Post post = postRepository.findById(Long.valueOf(postId))
//                .orElseThrow(() -> new RuntimeException("Post not found"));
//
//        if (likeRepository.existsByPostAndUser(post, token)) {
//            likeRepository.deleteByPostAndUser(post, token);
//            return ResponseEntity.ok(Map.of("liked", false));
//        } else {
//            Like like = new Like();
//            like.setPost(post);
//            like.setUser(token);
//            likeRepository.save(like);
//            return ResponseEntity.ok(Map.of("liked", true));
//        }
//    }

    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<Long> getLikesCount(@PathVariable Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return ResponseEntity.ok(likeRepository.countByPost(post));
    }

    @GetMapping("/{postId}/likes/check")
    public ResponseEntity<Boolean> checkIfLiked(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader
    ) throws UserPrincipalNotFoundException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header: Authorization header must start with 'Bearer '");
        }

        String token = authHeader.replace("Bearer ", "");
        ResponseEntity<?> checkIfLike = postService.checkIfLike(token, postId);
        return (ResponseEntity<Boolean>) ResponseEntity.ok(checkIfLike).getBody();
    }

    @Cacheable(value = "postsWithComments", key = "#postId")
    @GetMapping("/{postId}/with-comments")
    public ResponseEntity<PostDto> getPostWithComments(
            @PathVariable Long postId
//            @AuthenticationPrincipal UserDetails userDetails
    ) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostDto postDto = mapPostToDto(post);

        // Добавляем информацию о лайках
//        postDto.setLikesCount(likeRepository.countByPost(post));

        // Проверяем, лайкcнул ли текущий пользователь
//        if (userDetails != null) {
//            User currentUser = userRepository.findByEmail(userDetails.getUsername())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//            postDto.setIsLiked(likeRepository.existsByPostAndUser(post, currentUser));
//        } else {
//            postDto.setIsLiked(false);
//        }

        // Добавляем комментарии
//        postDto.setComments(commentService.getCommentsByPostId(postId));

        return ResponseEntity.ok(postDto);
    }

    private PostDto mapPostToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());


        dto.setAuthorName(post.getAuthor());
        dto.setAuthorName(String.valueOf(post.getAuthor()));
//        dto.setAuthorAvatarUrl(post.getAuthor().getAvatarUrl());

        return dto;
    }


}