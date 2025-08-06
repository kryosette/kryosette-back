package com.substring.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatAppBackendApplication {

	public static void main(String[] args)	{
		System.setProperty("jdk.tls.client.protocols", "TLSv1.3");
		SpringApplication.run(ChatAppBackendApplication.class, args);
	}

}
