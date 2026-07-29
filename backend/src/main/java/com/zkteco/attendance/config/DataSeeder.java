package com.zkteco.attendance.config;

import com.zkteco.attendance.entity.Operator;
import com.zkteco.attendance.entity.OperatorStatus;
import com.zkteco.attendance.entity.Permission;
import com.zkteco.attendance.entity.Role;
import com.zkteco.attendance.repository.OperatorRepository;
import com.zkteco.attendance.repository.PermissionRepository;
import com.zkteco.attendance.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Seeds reference data (permissions, SUPER_ADMIN role) and the initial
 * SUPER_ADMIN operator on first boot, using the app's own PasswordEncoder
 * bean so the hash is guaranteed valid.
 *
 * Reference data normally comes from Flyway's V1__init.sql (the MySQL/
 * production path). This seeder re-checks and fills in anything missing so
 * it also works standalone against the optional zero-install "h2" profile,
 * where Flyway is disabled and Hibernate creates the schema directly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "Admin@123";

    private static final List<String[]> DEFAULT_PERMISSIONS = Arrays.asList(
            new String[]{"DASHBOARD_VIEW", "View dashboard statistics", "DASHBOARD"},
            new String[]{"DEVICE_VIEW", "View devices", "DEVICE"},
            new String[]{"DEVICE_CREATE", "Add a new device", "DEVICE"},
            new String[]{"DEVICE_EDIT", "Edit device details", "DEVICE"},
            new String[]{"DEVICE_DELETE", "Delete a device", "DEVICE"},
            new String[]{"USER_VIEW", "View device users", "USER"},
            new String[]{"USER_CREATE", "Add a device user", "USER"},
            new String[]{"USER_EDIT", "Edit a device user", "USER"},
            new String[]{"USER_DELETE", "Delete device user(s)", "USER"},
            new String[]{"ROLE_VIEW", "View roles and permissions", "ROLE"},
            new String[]{"ROLE_MANAGE", "Create/edit roles and assign them", "ROLE"},
            new String[]{"ATTENDANCE_VIEW", "View attendance records", "ATTENDANCE"},
            new String[]{"ATTENDANCE_EXPORT", "Export attendance records to CSV", "ATTENDANCE"},
            new String[]{"COMMAND_VIEW", "View device command queue", "COMMAND"}
    );

    private final OperatorRepository operatorRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedPermissions();
        Role superAdminRole = seedSuperAdminRole();
        seedSuperAdminOperator(superAdminRole);
    }

    private void seedPermissions() {
        if (permissionRepository.count() > 0) {
            return;
        }
        for (String[] p : DEFAULT_PERMISSIONS) {
            Permission permission = new Permission();
            permission.setCode(p[0]);
            permission.setDescription(p[1]);
            permission.setModule(p[2]);
            permissionRepository.save(permission);
        }
        log.info("Seeded {} default permissions", DEFAULT_PERMISSIONS.size());
    }

    private Role seedSuperAdminRole() {
        return roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("SUPER_ADMIN");
            role.setDescription("Full access across all institutes");
            role.setPermissions(new HashSet<>(permissionRepository.findAll()));
            Role saved = roleRepository.save(role);
            log.info("Seeded SUPER_ADMIN role with {} permissions", saved.getPermissions().size());
            return saved;
        });
    }

    private void seedSuperAdminOperator(Role superAdminRole) {
        if (operatorRepository.existsByUsername(DEFAULT_USERNAME)) {
            return;
        }

        Operator operator = new Operator();
        operator.setUsername(DEFAULT_USERNAME);
        operator.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        operator.setFullName("System Administrator");
        operator.setEmail("admin@example.com");
        operator.setInstituteId(null);
        operator.setStatus(OperatorStatus.ACTIVE);
        operator.setRoles(Collections.singleton(superAdminRole));

        operatorRepository.save(operator);

        log.warn("Seeded default SUPER_ADMIN operator username='{}' password='{}' - change this immediately.",
                DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }
}
