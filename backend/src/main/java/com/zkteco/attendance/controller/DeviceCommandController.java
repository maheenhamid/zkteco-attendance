package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.command.DeviceCommandDTO;
import com.zkteco.attendance.dto.common.PageResponse;
import com.zkteco.attendance.entity.CommandStatus;
import com.zkteco.attendance.service.DeviceCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commands")
@RequiredArgsConstructor
public class DeviceCommandController {

    private final DeviceCommandService deviceCommandService;

    @GetMapping
    @PreAuthorize("hasAuthority('COMMAND_VIEW')")
    public PageResponse<DeviceCommandDTO> list(@RequestParam(required = false) Long deviceId,
                                                @RequestParam(required = false) CommandStatus status,
                                                @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return PageResponse.of(deviceCommandService.list(deviceId, status, pageable), DeviceCommandDTO::new);
    }
}
