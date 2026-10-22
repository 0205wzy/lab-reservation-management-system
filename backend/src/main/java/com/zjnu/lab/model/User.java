package com.zjnu.lab.model;
import lombok.Data;
@Data public class User { private Long id; private String username; private String passwordHash; private String realName; private String phone; private String role; private Integer status; }
