package com.substring.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class KryosetteChat {
	public static void main(String[] args)	{
		System.setProperty("jdk.tls.client.protocols", "TLSv1.3");
		SpringApplication.run(KryosetteChat.class, args);
	}
}
