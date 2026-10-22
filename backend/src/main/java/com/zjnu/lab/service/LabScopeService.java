package com.zjnu.lab.service;

import com.zjnu.lab.common.BusinessException;
import com.zjnu.lab.mapper.LabAssignmentMapper;
import com.zjnu.lab.model.Lab;
import com.zjnu.lab.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabScopeService {
    private final LabAssignmentMapper assignments;

    public boolean isLabAdmin() {
        return "LAB_ADMIN".equals(SecurityUtil.role());
    }

    /** Null means unrestricted; a list, including an empty one, is a manager's scope. */
    public List<Long> scope() {
        if (!isLabAdmin()) return null;
        List<Long> assignedLabs = assignments.findByUser(SecurityUtil.userId());
        return assignedLabs.isEmpty() ? List.of(-1L) : assignedLabs;
    }

    public void requireAccess(Long labId) {
        if (isLabAdmin() && !assignments.findByUser(SecurityUtil.userId()).contains(labId)) {
            throw new BusinessException("无权访问未分配的实验室");
        }
    }

    public void requireAccess(Lab lab) {
        if (lab == null) throw new BusinessException("实验室不存在");
        requireAccess(lab.getId());
    }
}
