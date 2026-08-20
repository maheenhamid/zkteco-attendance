package com.zkteco.attendance.service;

import com.zkteco.attendance.dto.deviceuser.DeviceUserDTO;
import com.zkteco.attendance.dto.deviceuser.DeviceUserRequest;
import com.zkteco.attendance.dto.deviceuser.ImportResultDTO;
import com.zkteco.attendance.entity.Device;
import com.zkteco.attendance.entity.DevicePrivilege;
import com.zkteco.attendance.entity.DeviceUser;
import com.zkteco.attendance.entity.Role;
import com.zkteco.attendance.entity.SyncStatus;
import com.zkteco.attendance.exception.BadRequestException;
import com.zkteco.attendance.exception.ResourceNotFoundException;
import com.zkteco.attendance.repository.DeviceRepository;
import com.zkteco.attendance.repository.DeviceUserRepository;
import com.zkteco.attendance.repository.RoleRepository;
import com.zkteco.attendance.security.SecurityUtils;
import com.zkteco.attendance.util.ZKTecoCommandBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceUserService {

    private static final int MAX_IMPORT_ERROR_MESSAGES = 50;
    private static final DateTimeFormatter EXPORT_TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] EXPORT_COLUMNS = {
            "Enroll No", "Full Name", "Card No", "Institute Id", "Department",
            "Device", "Device Privilege", "Sync Status", "Status", "Created At",
    };

    private final DeviceUserRepository deviceUserRepository;
    private final DeviceRepository deviceRepository;
    private final RoleRepository roleRepository;
    private final DeviceCommandService deviceCommandService;

    @Transactional(readOnly = true)
    public Page<DeviceUser> list(Long instituteId, Long classId, Long deviceId, String search, Pageable pageable) {
        return deviceUserRepository.findAll(buildFilterSpec(instituteId, classId, deviceId, search), pageable);
    }

    /** Same filters as {@link #list}, unpaginated - backs the Excel export button. */
    @Transactional(readOnly = true)
    public byte[] exportToExcel(Long instituteId, Long classId, Long deviceId, String search) {
        List<DeviceUser> users = deviceUserRepository.findAll(buildFilterSpec(instituteId, classId, deviceId, search));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");

            Row header = sheet.createRow(0);
            for (int i = 0; i < EXPORT_COLUMNS.length; i++) {
                header.createCell(i).setCellValue(EXPORT_COLUMNS[i]);
            }

            int rowIdx = 1;
            for (DeviceUser user : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.getEnrollNo());
                row.createCell(1).setCellValue(user.getFullName());
                row.createCell(2).setCellValue(user.getCardNo() != null ? user.getCardNo() : "");
                row.createCell(3).setCellValue(user.getInstituteId());
                row.createCell(4).setCellValue(user.getClassName() != null ? user.getClassName() : "");
                row.createCell(5).setCellValue(user.getDevice().getName());
                row.createCell(6).setCellValue(user.getDevicePrivilege().name());
                row.createCell(7).setCellValue(user.getSyncStatus().name());
                row.createCell(8).setCellValue(user.getStatus().name());
                row.createCell(9).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().format(EXPORT_TS_FORMAT) : "");
            }

            for (int i = 0; i < EXPORT_COLUMNS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate users Excel export", e);
        }
    }

    @Transactional
    public DeviceUser create(DeviceUserRequest request) {
        Long instituteId = SecurityUtils.resolveInstituteId(request.getInstituteId());
        if (instituteId == null) {
            throw new BadRequestException("Institute is required");
        }
        Device device = resolveDevice(request.getDeviceId(), instituteId);

        DeviceUser user = new DeviceUser();
        user.setInstituteId(instituteId);
        user.setClassId(request.getClassId());
        user.setClassName(request.getClassName());
        user.setFullName(request.getFullName());
        user.setEnrollNo(StringUtils.hasText(request.getEnrollNo()) ? request.getEnrollNo() : nextEnrollNo(device.getId()));
        user.setCardNo(request.getCardNo());
        user.setDevicePrivilege(request.getDevicePrivilege());
        user.setDevice(device);
        user.setRoles(resolveRoles(request.getRoleIds()));
        user.setSyncStatus(SyncStatus.PENDING);

        user = deviceUserRepository.save(user);
        enqueueSync(device, user);
        log.info("Created device user id={} enrollNo={} device={}", user.getId(), user.getEnrollNo(), device.getSerialNumber());
        return user;
    }

    /** Auto-assigns the next free PIN on the device (max existing numeric enrollNo + 1, starting at 1). */
    private String nextEnrollNo(Long deviceId) {
        int max = deviceUserRepository.findEnrollNosByDeviceId(deviceId).stream()
                .filter(StringUtils::hasText)
                .mapToInt(s -> {
                    try {
                        return Integer.parseInt(s.trim());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.valueOf(max + 1);
    }

    @Transactional
    public DeviceUser update(Long id, DeviceUserRequest request) {
        DeviceUser user = getOwned(id);
        Long instituteId = SecurityUtils.resolveInstituteId(request.getInstituteId());
        Device device = resolveDevice(request.getDeviceId(), instituteId != null ? instituteId : user.getInstituteId());

        if (SecurityUtils.isSuperAdmin() && instituteId != null) {
            user.setInstituteId(instituteId);
        }
        user.setClassId(request.getClassId());
        user.setClassName(request.getClassName());
        user.setFullName(request.getFullName());
        // enrollNo is the physical device PIN - only change it if the caller explicitly
        // provided one; never blank it out (the form doesn't collect it on edit).
        if (StringUtils.hasText(request.getEnrollNo())) {
            user.setEnrollNo(request.getEnrollNo());
        }
        user.setCardNo(request.getCardNo());
        user.setDevicePrivilege(request.getDevicePrivilege());
        user.setDevice(device);
        user.setRoles(resolveRoles(request.getRoleIds()));
        user.setSyncStatus(SyncStatus.PENDING);

        user = deviceUserRepository.save(user);
        enqueueSync(device, user);
        log.info("Updated device user id={} enrollNo={} device={}", user.getId(), user.getEnrollNo(), device.getSerialNumber());
        return user;
    }

    @Transactional
    public void delete(Long id) {
        DeviceUser user = getOwned(id);
        deviceCommandService.enqueue(user.getDevice(), "DATA DELETE USERINFO", ZKTecoCommandBuilder.userDelete(user.getEnrollNo()));
        deviceUserRepository.delete(user);
        log.info("Deleted device user id={} enrollNo={} (app + device delete command queued)", id, user.getEnrollNo());
    }

    @Transactional
    public int bulkDelete(List<Long> ids) {
        List<DeviceUser> users = deviceUserRepository.findAllById(ids);
        for (DeviceUser user : users) {
            assertOwnership(user);
            deviceCommandService.enqueue(user.getDevice(), "DATA DELETE USERINFO", ZKTecoCommandBuilder.userDelete(user.getEnrollNo()));
        }
        deviceUserRepository.deleteAll(users);
        log.info("Bulk deleted {} device user(s) by explicit selection", users.size());
        return users.size();
    }

    @Transactional
    public int bulkDeleteByFilter(Long instituteId, Long classId) {
        Long scopedInstituteId = SecurityUtils.resolveInstituteId(instituteId);
        if (scopedInstituteId == null) {
            throw new BadRequestException("Institute is required for a filtered bulk delete");
        }
        List<DeviceUser> users = deviceUserRepository.findAll(buildFilterSpec(scopedInstituteId, classId, null, null));
        for (DeviceUser user : users) {
            deviceCommandService.enqueue(user.getDevice(), "DATA DELETE USERINFO", ZKTecoCommandBuilder.userDelete(user.getEnrollNo()));
        }
        deviceUserRepository.deleteAll(users);
        log.info("Bulk deleted {} device user(s) by filter institute={} class={}", users.size(), scopedInstituteId, classId);
        return users.size();
    }

    @Transactional
    public DeviceUser resend(Long id) {
        DeviceUser user = getOwned(id);
        user.setSyncStatus(SyncStatus.PENDING);
        user = deviceUserRepository.save(user);
        enqueueSync(user.getDevice(), user);
        log.info("Resent device user id={} enrollNo={} to device={}", user.getId(), user.getEnrollNo(), user.getDevice().getSerialNumber());
        return user;
    }

    @Transactional
    public int resendUnsynced(Long instituteId, Long classId, Long deviceId) {
        Long scopedInstituteId = SecurityUtils.resolveInstituteId(instituteId);
        List<DeviceUser> users = deviceUserRepository.findAll(buildFilterSpec(scopedInstituteId, classId, deviceId, null));

        int resent = 0;
        for (DeviceUser user : users) {
            if (user.getSyncStatus() == SyncStatus.SYNCED) {
                continue;
            }
            user.setSyncStatus(SyncStatus.PENDING);
            deviceUserRepository.save(user);
            enqueueSync(user.getDevice(), user);
            resent++;
        }
        log.info("Resent {} unsynced device user(s) institute={} class={} device={}", resent, scopedInstituteId, classId, deviceId);
        return resent;
    }

    /**
     * classId/className mirror the manual Add User form's Institute -> Class ->
     * Device selection exactly (same resolved values, same optional-Class
     * behavior) so users created via Excel end up identical to manually-created
     * ones - including having a real classId, not just free text. When left
     * blank (matching manual add, where Class isn't required either), each
     * row's own "department/class" spreadsheet column is used as a text-only
     * fallback the way earlier versions of this import worked.
     */
    @Transactional
    public ImportResultDTO importFromExcel(MultipartFile file, Long instituteId, Long deviceId, Long classId, String className) {
        Long scopedInstituteId = SecurityUtils.resolveInstituteId(instituteId);
        if (scopedInstituteId == null) {
            throw new BadRequestException("Institute is required");
        }
        Device device = resolveDevice(deviceId, scopedInstituteId);

        ImportResultDTO result = new ImportResultDTO(0, 0, new ArrayList<>());

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // header row: userId, name, cardNumber, department/class
                }
                if (isBlankRow(row)) {
                    continue;
                }

                String userId = cellText(row.getCell(0));
                String name = cellText(row.getCell(1));
                String cardNumber = cellText(row.getCell(2));
                String department = cellText(row.getCell(3));

                String rowLabel = "Row " + (row.getRowNum() + 1);
                try {
                    importRow(scopedInstituteId, device, userId, name, cardNumber, department, classId, className);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (BadRequestException e) {
                    result.setErrorCount(result.getErrorCount() + 1);
                    addError(result, rowLabel + ": " + e.getMessage());
                }
            }
        } catch (IOException | RuntimeException e) {
            log.error("Failed to parse uploaded Excel file", e);
            throw new BadRequestException("Could not read the uploaded file - is it a valid .xlsx?");
        }

        log.info("Excel import finished: {} succeeded, {} failed (institute={}, device={}, classId={})",
                result.getSuccessCount(), result.getErrorCount(), scopedInstituteId, device.getId(), classId);
        return result;
    }

    private void importRow(Long instituteId, Device device, String userId, String name, String cardNumber,
                            String department, Long classId, String className) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(name)) {
            throw new BadRequestException("userId and name are required");
        }
        if (deviceUserRepository.findByDeviceIdAndEnrollNo(device.getId(), userId).isPresent()) {
            throw new BadRequestException("Duplicate userId '" + userId + "' on this device");
        }
        if (deviceUserRepository.cardNoTaken(instituteId, cardNumber)) {
            throw new BadRequestException("Duplicate cardNumber '" + cardNumber + "' in this institute");
        }

        DeviceUser user = new DeviceUser();
        user.setInstituteId(instituteId);
        user.setClassId(classId);
        user.setClassName(StringUtils.hasText(className) ? className : department);
        user.setFullName(name);
        user.setEnrollNo(userId);
        user.setCardNo(cardNumber);
        user.setDevicePrivilege(DevicePrivilege.COMMON);
        user.setDevice(device);
        user.setSyncStatus(SyncStatus.PENDING);

        user = deviceUserRepository.save(user);
        enqueueSync(device, user);
    }

    private boolean isBlankRow(Row row) {
        return !StringUtils.hasText(cellText(row.getCell(0))) && !StringUtils.hasText(cellText(row.getCell(1)));
    }

    private String cellText(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                double value = cell.getNumericCellValue();
                return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return cell.getStringCellValue() == null ? null : cell.getStringCellValue().trim();
        }
    }

    private void addError(ImportResultDTO result, String message) {
        if (result.getErrors().size() < MAX_IMPORT_ERROR_MESSAGES) {
            result.getErrors().add(message);
        }
    }

    @Transactional(readOnly = true)
    public DeviceUser getOwned(Long id) {
        DeviceUser user = deviceUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device user not found: " + id));
        assertOwnership(user);
        return user;
    }

    public static DeviceUserDTO toDTO(DeviceUser user) {
        return new DeviceUserDTO(user);
    }

    private Specification<DeviceUser> buildFilterSpec(Long instituteId, Long classId, Long deviceId, String search) {
        Long scopedInstituteId = SecurityUtils.resolveInstituteId(instituteId);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (scopedInstituteId != null) {
                predicates.add(cb.equal(root.get("instituteId"), scopedInstituteId));
            }
            if (classId != null) {
                predicates.add(cb.equal(root.get("classId"), classId));
            }
            if (deviceId != null) {
                predicates.add(cb.equal(root.get("device").get("id"), deviceId));
            }
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("enrollNo")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Device resolveDevice(Long deviceId, Long instituteId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + deviceId));
        if (!SecurityUtils.isSuperAdmin() && !device.getInstituteId().equals(instituteId)) {
            throw new BadRequestException("Selected device does not belong to your institute");
        }
        return device;
    }

    private Set<Role> resolveRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        if (roles.size() != new HashSet<>(roleIds).size()) {
            throw new BadRequestException("One or more role ids are invalid");
        }
        return roles;
    }

    private void enqueueSync(Device device, DeviceUser user) {
        deviceCommandService.enqueue(device, "DATA UPDATE USERINFO", ZKTecoCommandBuilder.userInfoUpdate(user));
    }

    private void assertOwnership(DeviceUser user) {
        if (!SecurityUtils.isSuperAdmin() && !user.getInstituteId().equals(SecurityUtils.currentInstituteId())) {
            throw new ResourceNotFoundException("Device user not found: " + user.getId());
        }
    }
}
