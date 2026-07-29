package com.zkteco.attendance.repository;

import com.zkteco.attendance.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
