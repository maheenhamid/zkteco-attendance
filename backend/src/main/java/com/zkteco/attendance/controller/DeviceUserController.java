package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.common.PageResponse;
import com.zkteco.attendance.dto.deviceuser.BulkDeleteByFilterRequest;
import com.zkteco.attendance.dto.deviceuser.BulkDeleteRequest;
import com.zkteco.attendance.dto.deviceuser.DeviceUserDTO;
import com.zkteco.attendance.dto.deviceuser.DeviceUserRequest;
import com.zkteco.attendance.dto.deviceuser.ImportResultDTO;
import com.zkteco.attendance.entity.DeviceUser;
import com.zkteco.attendance.service.DeviceUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class DeviceUserController {

    private final DeviceUserService deviceUserService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public PageResponse<DeviceUserDTO> list(@RequestParam(required = false) Long instituteId,
                                             @RequestParam(required = false) Long classId,
                                             @RequestParam(required = false) Long deviceId,
                                             @RequestParam(required = false) String search,
                                             @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.of(deviceUserService.list(instituteId, classId, deviceId, search, pageable), DeviceUserService::toDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public DeviceUserDTO get(@PathVariable Long id) {
        return DeviceUserService.toDTO(deviceUserService.getOwned(id));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('USER_EXPORT')")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long instituteId,
                                          @RequestParam(required = false) Long classId,
                                          @RequestParam(required = false) Long deviceId,
                                          @RequestParam(required = false) String search) {
        byte[] bytes = deviceUserService.exportToExcel(instituteId, classId, deviceId, search);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "users-export.xlsx");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<DeviceUserDTO> create(@Valid @RequestBody DeviceUserRequest request) {
        DeviceUser user = deviceUserService.create(request);
        return ResponseEntity.ok(DeviceUserService.toDTO(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public DeviceUserDTO update(@PathVariable Long id, @Valid @RequestBody DeviceUserRequest request) {
        return DeviceUserService.toDTO(deviceUserService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Map<String, Integer>> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        int deleted = deviceUserService.bulkDelete(request.getIds());
        return ResponseEntity.ok(Collections.singletonMap("deleted", deleted));
    }

    @PostMapping("/bulk-delete-by-filter")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Map<String, Integer>> bulkDeleteByFilter(@Valid @RequestBody BulkDeleteByFilterRequest request) {
        int deleted = deviceUserService.bulkDeleteByFilter(request.getInstituteId(), request.getClassId());
        return ResponseEntity.ok(Collections.singletonMap("deleted", deleted));
    }

    @PostMapping("/{id}/resend")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public DeviceUserDTO resend(@PathVariable Long id) {
        return DeviceUserService.toDTO(deviceUserService.resend(id));
    }

    @PostMapping("/resend-unsynced")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<Map<String, Integer>> resendUnsynced(@RequestParam(required = false) Long instituteId,
                                                                 @RequestParam(required = false) Long classId,
                                                                 @RequestParam(required = false) Long deviceId) {
        int resent = deviceUserService.resendUnsynced(instituteId, classId, deviceId);
        return ResponseEntity.ok(Collections.singletonMap("resent", resent));
    }

    @PostMapping(value = "/import-excel", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ImportResultDTO importExcel(@RequestParam("file") MultipartFile file,
                                        @RequestParam Long instituteId,
                                        @RequestParam Long deviceId,
                                        @RequestParam(required = false) Long classId,
                                        @RequestParam(required = false) String className) {
        return deviceUserService.importFromExcel(file, instituteId, deviceId, classId, className);
    }
}
