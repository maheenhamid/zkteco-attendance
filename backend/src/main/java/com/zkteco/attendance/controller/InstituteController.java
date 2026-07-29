package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.institute.ClassDTO;
import com.zkteco.attendance.dto.institute.InstituteDTO;
import com.zkteco.attendance.security.SecurityUtils;
import com.zkteco.attendance.service.InstituteApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/institutes")
@RequiredArgsConstructor
public class InstituteController {

    private final InstituteApiService instituteApiService;

    /** Non-super-admins only ever see their own institute, matching the operator's tenant scope. */
    @GetMapping
    public List<InstituteDTO> list() {
        List<InstituteDTO> all = instituteApiService.listInstitutes();
        if (SecurityUtils.isSuperAdmin()) {
            return all;
        }
        Long instituteId = SecurityUtils.currentInstituteId();
        return all.stream().filter(i -> i.getId() != null && i.getId().equals(instituteId)).collect(Collectors.toList());
    }

    @GetMapping("/{id}/classes")
    public List<ClassDTO> classes(@PathVariable Long id) {
        return instituteApiService.listClasses(id);
    }
}
