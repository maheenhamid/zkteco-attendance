package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.department.DepartmentDTO;
import com.zkteco.attendance.dto.department.DepartmentRequest;
import com.zkteco.attendance.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_VIEW')")
    public List<DepartmentDTO> list(@RequestParam(required = false) Long instituteId) {
        return departmentService.list(instituteId).stream().map(DepartmentDTO::new).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_CREATE')")
    public DepartmentDTO create(@Valid @RequestBody DepartmentRequest request) {
        return new DepartmentDTO(departmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_EDIT')")
    public DepartmentDTO update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return new DepartmentDTO(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }
}
