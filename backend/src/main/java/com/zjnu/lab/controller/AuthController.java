package com.zjnu.lab.controller;
import com.zjnu.lab.common.*; import com.zjnu.lab.mapper.UserMapper; import com.zjnu.lab.model.User; import com.zjnu.lab.security.JwtUtil; import com.zjnu.lab.util.SecurityUtil;
import jakarta.validation.constraints.NotBlank; import lombok.RequiredArgsConstructor; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/auth") @RequiredArgsConstructor public class AuthController {
 private final UserMapper users; private final PasswordEncoder encoder; private final JwtUtil jwt;
 public record Login(@NotBlank String username,@NotBlank String password){}
 @PostMapping("/login") public ApiResponse<?> login(@RequestBody Login p){User u=users.findByUsername(p.username());if(u==null||u.getStatus()!=1||!encoder.matches(p.password(),u.getPasswordHash()))throw new BusinessException("用户名或密码错误");return ApiResponse.ok(Map.of("token",jwt.create(u.getId(),u.getUsername(),u.getRole()),"user",safe(u)));}
 @GetMapping("/me") public ApiResponse<?> me(){return ApiResponse.ok(safe(users.findByUsername(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName())));}
 private Map<String,Object> safe(User u){return Map.of("id",u.getId(),"username",u.getUsername(),"realName",u.getRealName(),"role",u.getRole(),"permissions",permissions(u.getRole()));}
 private List<String> permissions(String role){return switch(role){case "SYSTEM_ADMIN"->List.of("dashboard","lab:view","lab:manage","device:view","device:manage","repair:handle","report:view","user:manage","assignment:manage");case "LAB_ADMIN"->List.of("dashboard","lab:view","lab:manage","device:view","device:manage","reservation:audit","repair:handle","report:view");default->List.of("dashboard","lab:view","device:view","reservation:apply","repair:apply");};}
}
