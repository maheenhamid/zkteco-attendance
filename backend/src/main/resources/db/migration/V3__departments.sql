-- =====================================================================
-- Departments: unlike institutes/classes (resolved from the external
-- Shebashikkha API), departments are managed entirely inside this app.
-- Each department belongs to exactly one institute (institute_id is a
-- plain external id, same convention as devices.institute_id /
-- device_users.institute_id - there is still no local institute table).
-- =====================================================================

CREATE TABLE departments (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    institute_id BIGINT NOT NULL,
    name         VARCHAR(128) NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_departments_institute_name UNIQUE (institute_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_departments_institute_id ON departments(institute_id);

INSERT INTO permissions (code, description, module) VALUES
    ('DEPARTMENT_VIEW',   'View departments',              'DEPARTMENT'),
    ('DEPARTMENT_CREATE', 'Add a new department',          'DEPARTMENT'),
    ('DEPARTMENT_EDIT',   'Edit a department',             'DEPARTMENT'),
    ('DEPARTMENT_DELETE', 'Delete a department',           'DEPARTMENT');

INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), id
FROM permissions
WHERE code IN ('DEPARTMENT_VIEW', 'DEPARTMENT_CREATE', 'DEPARTMENT_EDIT', 'DEPARTMENT_DELETE');
