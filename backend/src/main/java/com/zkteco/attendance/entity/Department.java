package com.zkteco.attendance.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * A department created and managed entirely within this app, scoped to one
 * institute. Unlike institutes (always resolved from the external
 * Shebashikkha API), there is no external source of truth for departments -
 * this table is it. {@code instituteId} is a plain external id, the same
 * convention {@link Device} and {@link DeviceUser} already use.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institute_id", nullable = false)
    private Long instituteId;

    @Column(nullable = false, length = 128)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
