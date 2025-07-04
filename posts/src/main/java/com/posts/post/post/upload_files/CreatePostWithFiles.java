//package com.posts.post.post.upload_files;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//
//@RestController
//@RequestMapping("posts/files")
//@RequiredArgsConstructor
//public class PostFileUploadController {
//
//    private static final String UPLOAD_DIRECTORY = "post_uploads";
//    private final PostFileRepository fileRepository;
//
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<PostFileUploadResponse> uploadPostFile(
//            @RequestPart("file") MultipartFile file) throws IOException {
//
//        try {
//            if (file.isEmpty()) {
//                return ResponseEntity.badRequest().build();
//            }
//
//            String originalFilename = file.getOriginalFilename();
//            String fileExtension = originalFilename != null && originalFilename.contains(".")
//                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
//                    : "";
//
//            String uniqueFilename = UUID.randomUUID() + fileExtension;
//            Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
//
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            Path filePath = uploadPath.resolve(uniqueFilename);
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//            PostFile fileEntity = PostFile.builder()
//                    .filename(originalFilename)
//                    .uniqueFilename(uniqueFilename)
//                    .fileSize(file.getSize())
//                    .fileType(file.getContentType())
//                    .fileUrl("/" + UPLOAD_DIRECTORY + "/" + uniqueFilename)
//                    .build();
//
//            fileRepository.save(fileEntity);
//
//            return ResponseEntity.status(HttpStatus.CREATED).body(
//                    PostFileUploadResponse.fromEntity(fileEntity)
//            );
//
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//}