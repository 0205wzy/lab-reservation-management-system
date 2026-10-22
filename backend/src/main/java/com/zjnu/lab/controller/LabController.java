package com.zjnu.lab.controller;
import com.zjnu.lab.common.*; import com.zjnu.lab.mapper.LabMapper; import com.zjnu.lab.model.Lab; import com.zjnu.lab.service.LabScopeService; import com.zjnu.lab.util.SecurityUtil; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor public class LabController { private final LabMapper mapper; private final LabScopeService scope;
 @GetMapping("/labs") public ApiResponse<?> list(@RequestParam(required=false)String keyword){return ApiResponse.ok(mapper.list(keyword,scope.scope()));}
 @GetMapping("/labs/{id}") public ApiResponse<?> get(@PathVariable Long id){Lab lab=mapper.get(id);scope.requireAccess(lab);return ApiResponse.ok(lab);}
 @PostMapping("/manage/labs") public ApiResponse<?> add(@RequestBody Lab v){if(!"SYSTEM_ADMIN".equals(SecurityUtil.role()))throw new BusinessException("只有系统管理员可以新增实验室");v.setOpenStatus(v.getOpenStatus()==null?1:v.getOpenStatus());mapper.insert(v);return ApiResponse.ok(v);}
 @PutMapping("/manage/labs/{id}") public ApiResponse<?> update(@PathVariable Long id,@RequestBody Lab v){scope.requireAccess(mapper.get(id));v.setId(id);mapper.update(v);return ApiResponse.ok(v);}
 @DeleteMapping("/manage/labs/{id}") public ApiResponse<?> disable(@PathVariable Long id){scope.requireAccess(mapper.get(id));mapper.disable(id);return ApiResponse.ok();}
}
