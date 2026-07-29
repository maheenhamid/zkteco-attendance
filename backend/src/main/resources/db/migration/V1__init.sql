-- =====================================================================
-- ZKTeco F18 Attendance Management System - initial schema
-- NOTE: there is intentionally NO institute/class table. Institutes and
-- classes are always resolved from the external Shebashikkha API; only
-- their numeric id is stored here (institute_id / class_id columns).
-- =====================================================================

CREATE TABLE permissions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    module      VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_permissions_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE operators (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL,
    password     VARCHAR(100) NOT NULL,
    full_name    VARCHAR(128) NOT NULL,
    email        VARCHAR(128),
    institute_id BIGINT NULL,
    status       VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_operators_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE operator_roles (
    operator_id BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    PRIMARY KEY (operator_id, role_id),
    CONSTRAINT fk_operator_roles_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT fk_operator_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE devices (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number    VARCHAR(64)  NOT NULL,
    name             VARCHAR(128) NOT NULL,
    institute_id     BIGINT NULL,
    ip_address       VARCHAR(45),
    port             INT,
    location         VARCHAR(255),
    status           VARCHAR(16)  NOT NULL DEFAULT 'OFFLINE',
    last_heartbeat   DATETIME NULL,
    firmware_version VARCHAR(64),
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_devices_serial_number UNIQUE (serial_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_devices_institute_id ON devices(institute_id);

CREATE TABLE device_users (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    institute_id     BIGINT NOT NULL,
    class_id         BIGINT NULL,
    class_name       VARCHAR(128),
    full_name        VARCHAR(128) NOT NULL,
    enroll_no        VARCHAR(32)  NOT NULL,
    card_no          VARCHAR(32),
    device_privilege VARCHAR(16)  NOT NULL DEFAULT 'COMMON',
    device_id        BIGINT NOT NULL,
    sync_status      VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    status            VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_device_users_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT uq_device_users_device_enroll UNIQUE (device_id, enroll_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_device_users_institute_id ON device_users(institute_id);
CREATE INDEX idx_device_users_class_id ON device_users(class_id);
CREATE INDEX idx_device_users_full_name ON device_users(full_name);

CREATE TABLE attendance_logs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    enroll_no     VARCHAR(32)  NOT NULL,
    device_user_id BIGINT NULL,
    device_id     BIGINT NOT NULL,
    institute_id  BIGINT NULL,
    punch_time    DATETIME NOT NULL,
    punch_type    VARCHAR(16),
    verify_mode   VARCHAR(16),
    raw_line      VARCHAR(512),
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_logs_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_logs_device_user FOREIGN KEY (device_user_id) REFERENCES device_users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_attendance_logs_institute_id ON attendance_logs(institute_id);
CREATE INDEX idx_attendance_logs_device_id ON attendance_logs(device_id);
CREATE INDEX idx_attendance_logs_enroll_no ON attendance_logs(enroll_no);
CREATE INDEX idx_attendance_logs_punch_time ON attendance_logs(punch_time);

CREATE TABLE device_commands (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id     BIGINT NOT NULL,
    command_type  VARCHAR(64)  NOT NULL,
    command_text  VARCHAR(1024) NOT NULL,
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    response_text VARCHAR(255),
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at       DATETIME NULL,
    executed_at   DATETIME NULL,
    CONSTRAINT fk_device_commands_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_device_commands_device_status ON device_commands(device_id, status);

-- =====================================================================
-- Reference data: permissions + SUPER_ADMIN role
-- The default SUPER_ADMIN operator account is seeded at application
-- startup (see DataSeeder) so its password is BCrypt-encoded by the
-- same PasswordEncoder bean the app authenticates with, instead of a
-- hand-computed hash baked into this migration.
-- =====================================================================

INSERT INTO permissions (code, description, module) VALUES
    ('DASHBOARD_VIEW',   'View dashboard statistics',        'DASHBOARD'),
    ('DEVICE_VIEW',      'View devices',                      'DEVICE'),
    ('DEVICE_CREATE',    'Add a new device',                  'DEVICE'),
    ('DEVICE_EDIT',      'Edit device details',                'DEVICE'),
    ('DEVICE_DELETE',    'Delete a device',                    'DEVICE'),
    ('USER_VIEW',        'View device users',                  'USER'),
    ('USER_CREATE',      'Add a device user',                  'USER'),
    ('USER_EDIT',        'Edit a device user',                 'USER'),
    ('USER_DELETE',      'Delete device user(s)',              'USER'),
    ('ROLE_VIEW',        'View roles and permissions',         'ROLE'),
    ('ROLE_MANAGE',      'Create/edit roles and assign them',  'ROLE'),
    ('ATTENDANCE_VIEW',  'View attendance records',            'ATTENDANCE'),
    ('ATTENDANCE_EXPORT','Export attendance records to CSV',   'ATTENDANCE'),
    ('COMMAND_VIEW',     'View device command queue',          'COMMAND');

INSERT INTO roles (name, description) VALUES
    ('SUPER_ADMIN', 'Full access across all institutes');

INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), id FROM permissions;
