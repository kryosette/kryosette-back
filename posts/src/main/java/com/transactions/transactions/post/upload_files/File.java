package com.transactions.transactions.post.upload_files;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String uniqueFilename;
    private long fileSize;
    private String fileType;
    private String fileUrl;
}
