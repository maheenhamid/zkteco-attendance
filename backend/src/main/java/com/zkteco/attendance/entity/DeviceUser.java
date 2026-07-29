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

/**
 * A biometric-enrolled person shown on the "User Management" page.
 * Distinct from {@link Operator}, which is a panel-login account.
 */
@Entity
@Table(name = "device_users")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"device", "roles"})
public class DeviceUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institute_id", nullable = false)
    private Long instituteId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "class_name", length = 128)
    private String className;

    @Column(name = "full_name", nullable = false, length = 128)
    private String fullName;

    /** The PIN enrolled on the ZKTeco device. */
    @Column(name = "enroll_no", nullable = false, length = 32)
    private String enrollNo;

    @Column(name = "card_no", length = 32)
    private String cardNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_privilege", nullable = false, length = 16)
    private DevicePrivilege devicePrivilege = DevicePrivilege.COMMON;

    // EAGER: DeviceUserDTO always needs device.name and mapping happens in the
    // controller, after the service's transaction/session has already closed.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    // EAGER: same rationale as `device` above - DeviceUserDTO reads this after
    // the service transaction/session has already closed.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "device_user_roles",
            joinColumns = @JoinColumn(name = "device_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 16)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OperatorStatus status = OperatorStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
