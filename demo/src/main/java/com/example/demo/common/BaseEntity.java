package com.example.demo.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Abstract base class for all JPA entities providing common audit fields.
 * Implements the "Audited Entity" pattern for tracking creation and modification timestamps.
 *
 * <p>Key features:
 * <ul>
 *   <li>Auto-populates creation timestamp on persist</li>
 *   <li>Auto-updates modification timestamp on update</li>
 *   <li>Designed for extension by concrete entity classes</li>
 * </ul>
 *
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 * @see javax.persistence.MappedSuperclass
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * The exact date and time when the entity was initially persisted to the database.
     * Automatically set by Spring Data JPA auditing infrastructure during the first save operation.
     *
     * <p>Characteristics:
     * <ul>
     *   <li>Never nullable</li>
     *   <li>Immutable after creation (updatable=false)</li>
     *   <li>Represents server-side timestamp in UTC</li>
     * </ul>
     *
     * @see org.springframework.data.annotation.CreatedDate
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * The exact date and time when the entity was last modified in the database.
     * Automatically updated by Spring Data JPA auditing infrastructure on each update.
     *
     * <p>Characteristics:
     * <ul>
     *   <li>Nullable until first update occurs</li>
     *   <li>Not set during initial insert (insertable=false)</li>
     *   <li>Represents server-side timestamp in UTC</li>
     * </ul>
     *
     * @see org.springframework.data.annotation.LastModifiedDate
     */
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}