package com.zkteco.attendance.service;

import com.zkteco.attendance.dto.department.DepartmentRequest;
import com.zkteco.attendance.entity.Department;
import com.zkteco.attendance.exception.BadRequestException;
import com.zkteco.attendance.exception.ResourceNotFoundException;
import com.zkteco.attendance.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<Department> list(Long instituteId) {
        if (instituteId == null) {
            return Collections.emptyList();
        }
        return departmentRepository.findByInstituteIdOrderByNameAsc(instituteId);
    }

    @Transactional
    public Department create(DepartmentRequest request) {
        if (departmentRepository.existsByInstituteIdAndNameIgnoreCase(request.getInstituteId(), request.getName())) {
            throw new BadRequestException("A department with this name already exists for this institute");
        }
        Department department = new Department();
        department.setInstituteId(request.getInstituteId());
        department.setName(request.getName());
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));

        boolean changingInstituteOrName = !department.getInstituteId().equals(request.getInstituteId())
                || !department.getName().equalsIgnoreCase(request.getName());
        if (changingInstituteOrName
                && departmentRepository.existsByInstituteIdAndNameIgnoreCase(request.getInstituteId(), request.getName())) {
            throw new BadRequestException("A department with this name already exists for this institute");
        }

        department.setInstituteId(request.getInstituteId());
        department.setName(request.getName());
        return departmentRepository.save(department);
    }

    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        departmentRepository.delete(department);
    }
}
