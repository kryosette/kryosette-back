package com.example.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class DatabaseConfig {

    @Autowired
    private HikariDataSource hikariDataSource;

    @EventListener(ContextClosedEvent.class)
    public void closeDataSource() {
        if (hikariDataSource != null) {
            System.out.println("Closing HikariCP DataSource...");
            hikariDataSource.close();
            System.out.println("HikariCP DataSource closed.");
        }
    }
}
