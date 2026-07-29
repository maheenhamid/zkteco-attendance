package com.zkteco.attendance.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "operators")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "roles")
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "full_name", nullable = false, length = 128)
    private String fullName;

    @Column(length = 128)
    private String email;

    /** Null only for SUPER_ADMIN operators who can see every institute. */
    @Column(name = "institute_id")
    private Long instituteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OperatorStatus status = OperatorStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "operator_roles",
            joinColumns = @JoinColumn(name = "operator_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isSuperAdmin() {
        return roles.stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));
    }
}
