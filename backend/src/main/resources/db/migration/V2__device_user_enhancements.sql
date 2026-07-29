-- =====================================================================
-- DeviceUser (card/biometric user) feature expansion.
-- Additive only: does not touch roles / role_permissions / operator_roles,
-- so the existing Operator Role & Permission module is unaffected.
-- =====================================================================

-- Item 1: DeviceUser <-> Role (system-role tagging for card users, separate
-- from device_users.device_privilege which stays the on-device privilege).
CREATE TABLE device_user_roles (
    device_user_id BIGINT NOT NULL,
    role_id        BIGINT NOT NULL,
    PRIMARY KEY (device_user_id, role_id),
    CONSTRAINT fk_device_user_roles_user FOREIGN KEY (device_user_id) REFERENCES device_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_device_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Item 2: Excel import duplicate-checking looks up card_no per institute;
-- speeds up existsByInstituteIdAndCardNo.
CREATE INDEX idx_device_users_institute_card_no ON device_users(institute_id, card_no);
