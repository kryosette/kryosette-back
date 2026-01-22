package com.posts.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.posts.post.post")
@EnableScheduling
public class KryosettePosts {

	public static void main(String[] args) {
		System.setProperty("jdk.tls.client.protocols", "TLSv1.3");
		SpringApplication.run(KryosettePosts.class, args);
	}

}
