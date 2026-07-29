package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.common.PageResponse;
import com.zkteco.attendance.dto.device.DeviceDTO;
import com.zkteco.attendance.dto.device.DeviceRequest;
import com.zkteco.attendance.entity.Device;
import com.zkteco.attendance.entity.DeviceStatus;
import com.zkteco.attendance.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @PreAuthorize("hasAuthority('DEVICE_VIEW')")
    public PageResponse<DeviceDTO> list(@RequestParam(required = false) Long instituteId,
                                         @RequestParam(required = false) DeviceStatus status,
                                         @RequestParam(required = false) String search,
                                         @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.of(deviceService.list(instituteId, status, search, pageable), DeviceService::toDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEVICE_VIEW')")
    public DeviceDTO get(@PathVariable Long id) {
        return DeviceService.toDTO(deviceService.getOwned(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEVICE_CREATE')")
    public ResponseEntity<DeviceDTO> create(@Valid @RequestBody DeviceRequest request) {
        Device device = deviceService.create(request);
        return ResponseEntity.ok(DeviceService.toDTO(device));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEVICE_EDIT')")
    public DeviceDTO update(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        return DeviceService.toDTO(deviceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEVICE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pull-users")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<Void> pullUsers(@PathVariable Long id) {
        deviceService.pullUsers(id);
        return ResponseEntity.accepted().build();
    }
}
