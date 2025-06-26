//package com.transactions.transactions.post.upload_files;
//
//import com.example.demo.security.id_generator.SUUID2;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
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
//    @PostMapping("/upload")
//    public ResponseEntity<FileUploadResponse> uploadFile(
//            @RequestParam("file") MultipartFile file
//            ) throws IOException {
//       try {
//           String originalFilename = file.getOriginalFilename();
//
//           String fileExtension = "";
//           if (originalFilename != null && !originalFilename.isEmpty()) {
//               Path path = Paths.get(originalFilename);
//               String fileName = path.getFileName().toString();
//               int dotIndex = fileName.lastIndexOf(".");
//               if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
//                   fileExtension = fileName.substring(dotIndex + 1);
//               }
//           }
//           String uniqueFilename = SUUID2.generateId() + fileExtension;
//
//           Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
//           if (!Files.exists(uploadPath)) {
//               Files.createDirectories(uploadPath);
//           }
//           Path filePath = uploadPath.resolve(uniqueFilename);
//           Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//           File fileEntity = new File();
//           fileEntity.setFilename(originalFilename);
//           fileEntity.setUniqueFilename(uniqueFilename);
//           fileEntity.setFileSize(file.getSize());
//           fileEntity.setFileType(file.getContentType());
//           fileEntity.setFileUrl("/" + UPLOAD_DIRECTORY + "/" + uniqueFilename);
//
//           fileRepository.save(fileEntity);
//
//           FileUploadResponse response = new FileUploadResponse();
//           response.setFilename(originalFilename);
//           response.setUniqueFilename(uniqueFilename);
//           response.setFileSize(file.getSize());
//           response.setFileType(file.getContentType());
//           response.setFileUrl("/" + UPLOAD_DIRECTORY + "/" + uniqueFilename);
//
//           return ResponseEntity.status(HttpStatus.CREATED).build();
//       } catch (IOException e) {
//           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//       }
//    }
//}
