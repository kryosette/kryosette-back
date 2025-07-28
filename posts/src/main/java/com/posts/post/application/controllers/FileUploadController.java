//package com.posts.post.application.controllers;
//
//import com.posts.post.domain.model.File;
//import com.posts.post.domain.repositories.FileRepository;
//import com.posts.post.domain.responses.FileUploadResponse;
//import com.posts.post.post.upload_files.SUUID2;
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
//@RequestMapping("files")
//@RequiredArgsConstructor
//public class FileUploadController {
//
//    private static final String UPLOAD_DIRECTORY = "uploads";
//    private final FileRepository fileRepository;
//
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<FileUploadResponse> uploadFile(
//            @RequestPart("file") MultipartFile file) throws IOException {
//
//        try {
//            if (file.isEmpty()) {
//                return ResponseEntity.badRequest().build();
//            }
//
//            String originalFilename = file.getOriginalFilename();
//            String fileExtension = "";
//
//            if (originalFilename != null && originalFilename.contains(".")) {
//                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
//            }
//
//            String uniqueFilename = SUUID2.generateId() + fileExtension;
//            Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
//
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            Path filePath = uploadPath.resolve(uniqueFilename);
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//            File fileEntity = new File();
//            fileEntity.setFilename(originalFilename);
//            fileEntity.setUniqueFilename(uniqueFilename);
//            fileEntity.setFileSize(file.getSize());
//            fileEntity.setFileType(file.getContentType());
//            fileEntity.setFileUrl("/" + UPLOAD_DIRECTORY + "/" + uniqueFilename);
//
//            fileRepository.save(fileEntity);
//
//            FileUploadResponse response = new FileUploadResponse();
//            response.setFilename(originalFilename);
//            response.setUniqueFilename(uniqueFilename);
//            response.setFileSize(file.getSize());
//            response.setFileType(file.getContentType());
//            response.setFileUrl("/" + UPLOAD_DIRECTORY + "/" + uniqueFilename);
//
//            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//}