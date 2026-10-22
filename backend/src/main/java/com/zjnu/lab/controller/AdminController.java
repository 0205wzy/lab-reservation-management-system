package com.zjnu.lab.controller;

import com.zjnu.lab.common.ApiResponse;
import com.zjnu.lab.common.BusinessException;
import com.zjnu.lab.mapper.LabAssignmentMapper;
import com.zjnu.lab.mapper.LabMapper;
import com.zjnu.lab.mapper.UserMapper;
import com.zjnu.lab.model.User;
import com.zjnu.lab.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private static final Set<String> ROLES = Set.of("USER", "LAB_ADMIN", "SYSTEM_ADMIN");
    private final UserMapper users;
    private final LabMapper labs;
    private final LabAssignmentMapper assignments;

    @GetMapping("/users")
    public ApiResponse<?> list() {
        return ApiResponse.ok(users.list());
    }

    @PutMapping("/users/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody User user) {
        if (id.equals(SecurityUtil.userId())) throw new BusinessException("不能修改当前登录管理员");
        User current = users.getById(id);
        if (current == null) throw new BusinessException("用户不存在");
        if (user.getRole() != null && !ROLES.contains(user.getRole())) throw new BusinessException("用户角色无效");
        if ("SYSTEM_ADMIN".equals(user.getRole()) && !"SYSTEM_ADMIN".equals(current.getRole())
                && users.systemAdminCount() > 0) {
            throw new BusinessException("系统管理员账号只能保留一个");
        }
        boolean demote = user.getRole() != null && !user.getRole().equals("SYSTEM_ADMIN");
        boolean disable = user.getStatus() != null && user.getStatus() == 0;
        if ("SYSTEM_ADMIN".equals(current.getRole()) && (disable || demote) && users.activeSystemAdmins() <= 1) {
            throw new BusinessException("不能禁用或降级最后一个系统管理员");
        }
        if (user.getRole() != null && "LAB_ADMIN".equals(user.getRole())
                && !"LAB_ADMIN".equals(current.getRole()) && users.assignmentCount(id) == 0) {
            throw new BusinessException("请先为实验室管理员分配实验室");
        }
        if (user.getRole() != null && !"LAB_ADMIN".equals(user.getRole())) {
            assignments.deleteByUser(id);
        }
        user.setId(id);
        users.update(user);
        return ApiResponse.ok();
    }

    @GetMapping("/lab-assignments")
    public ApiResponse<?> labAssignments() {
        return ApiResponse.ok(assignments.list());
    }

    public record LabAssignmentRequest(List<Long> labIds) {}

    @PutMapping("/users/{id}/labs")
    @Transactional
    public ApiResponse<?> setLabAssignments(@PathVariable Long id, @RequestBody LabAssignmentRequest request) {
        if (id.equals(SecurityUtil.userId())) throw new BusinessException("不能修改自己的实验室权限");
        User user = users.getById(id);
        if (user == null || "SYSTEM_ADMIN".equals(user.getRole())) throw new BusinessException("系统管理员不使用实验室范围分配");
        List<Long> labIds = request.labIds() == null ? List.of() : request.labIds().stream().distinct().toList();
        if (labIds.isEmpty() || labIds.size() > 2) throw new BusinessException("每位实验室管理员必须分配1至2个实验室");
        if (labs.countByIds(labIds) != labIds.size()) throw new BusinessException("所选实验室不存在");
        if (labs.assignedToOtherManagers(labIds, id) > 0) throw new BusinessException("一个实验室只能分配给一位负责人，请先调整现有分配");
        assignments.deleteByUser(id);
        for (Long labId : labIds) assignments.insert(id, labId);
        return ApiResponse.ok(assignments.findByUser(id));
    }
}
