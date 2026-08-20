-- =====================================================================
-- New permission for the User Management page's Excel export button.
-- Separate from USER_VIEW, mirroring how ATTENDANCE_EXPORT is kept apart
-- from ATTENDANCE_VIEW.
-- =====================================================================

INSERT INTO permissions (code, description, module) VALUES
    ('USER_EXPORT', 'Export device users to Excel', 'USER');

INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), id
FROM permissions
WHERE code = 'USER_EXPORT';
