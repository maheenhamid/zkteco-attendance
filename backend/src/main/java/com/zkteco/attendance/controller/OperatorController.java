package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.operator.AssignRoleRequest;
import com.zkteco.attendance.dto.operator.OperatorDTO;
import com.zkteco.attendance.dto.operator.OperatorRequest;
import com.zkteco.attendance.service.OperatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operators")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorService operatorService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public List<OperatorDTO> list() {
        return operatorService.list().stream().map(OperatorDTO::new).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public OperatorDTO create(@Valid @RequestBody OperatorRequest request) {
        return new OperatorDTO(operatorService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public OperatorDTO update(@PathVariable Long id, @Valid @RequestBody OperatorRequest request) {
        return new OperatorDTO(operatorService.update(id, request));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public OperatorDTO assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRoleRequest request) {
        return new OperatorDTO(operatorService.assignRoles(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public void delete(@PathVariable Long id) {
        operatorService.delete(id);
    }
}
