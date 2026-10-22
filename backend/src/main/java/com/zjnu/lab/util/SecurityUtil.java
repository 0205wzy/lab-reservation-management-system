package com.zjnu.lab.util;
import org.springframework.security.core.Authentication; import org.springframework.security.core.context.SecurityContextHolder;
public final class SecurityUtil { private SecurityUtil(){} public static Long userId(){Authentication a=SecurityContextHolder.getContext().getAuthentication(); return (Long)a.getDetails();} public static String role(){return SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority().replace("ROLE_","");} }
