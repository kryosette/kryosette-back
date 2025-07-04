package com.posts.post.post.upload_files;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private static final String UPLOAD_DIRECTORY = "uploads";
    private final FileRepository fileRepository;

    public File uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFilename != null && !originalFilename.isEmpty()) {
            Path path = Paths.get(originalFilename);
            String fileName = path.getFileName().toString();
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                fileExtension = fileName.substring(dotIndex + 1);
            }
        }

        String uniqueFilename = SUUID2.generateId() + "." + fileExtension;
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        File fileEntity = new File();
        fileEntity.setFilename(originalFilename);
        fileEntity.setUniqueFilename(uniqueFilename);
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFileType(file.getContentType());
        fileEntity.setFileUrl("/" + UPLOAD_DIRECTORY + "/" + uniqueFilename);

        return fileRepository.save(fileEntity);
    }
}
