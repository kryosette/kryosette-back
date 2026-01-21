package com.example.demo;

import com.example.demo.domain.model.user.role.Role;
import com.example.demo.domain.repositories.communication.user.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableCaching
public class KryosetteAuth {

	public static void main(String[] args) {
		System.setProperty("jdk.tls.client.protocols", "TLSv1.3");
        System.setProperty("java.net.preferIPv6Addresses", "true");
		SpringApplication.run(KryosetteAuth.class, args);
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository) {
		return args -> {
			if (roleRepository.findByName("USER").isEmpty()) {
				roleRepository.save(
						Role.builder().name("USER").build()
				);
			}
		};
	}
}
