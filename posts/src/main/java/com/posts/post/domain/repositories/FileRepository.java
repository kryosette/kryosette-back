package com.posts.post.domain.repositories;

import com.posts.post.domain.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, String> {
}