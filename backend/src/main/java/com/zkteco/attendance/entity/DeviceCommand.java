package com.zkteco.attendance.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_commands")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "device")
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER: DeviceCommandDTO always needs device.name and mapping happens in
    // the controller, after the service's transaction/session has already closed.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "command_type", nullable = false, length = 64)
    private String commandType;

    @Column(name = "command_text", nullable = false, length = 1024)
    private String commandText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CommandStatus status = CommandStatus.PENDING;

    @Column(name = "response_text", length = 255)
    private String responseText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;
}
