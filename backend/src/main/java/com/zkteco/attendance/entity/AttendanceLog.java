package com.zkteco.attendance.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_logs")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"device", "deviceUser"})
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enroll_no", nullable = false, length = 32)
    private String enrollNo;

    // EAGER: AttendanceDTO always needs these and mapping happens in the
    // controller, after the service's transaction/session has already closed.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_user_id")
    private DeviceUser deviceUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "institute_id")
    private Long instituteId;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    /** Raw ZKTeco status code (0=Check In,1=Check Out,2=Break Out,3=Break In,4=OT In,5=OT Out). */
    @Column(name = "punch_type", length = 16)
    private String punchType;

    /** Raw ZKTeco verify mode (0=Password,1=Fingerprint,2=Card,15=Face...). */
    @Column(name = "verify_mode", length = 16)
    private String verifyMode;

    @Column(name = "raw_line", length = 512)
    private String rawLine;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
