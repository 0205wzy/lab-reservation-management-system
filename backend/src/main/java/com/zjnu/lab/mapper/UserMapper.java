package com.zjnu.lab.mapper;
import com.zjnu.lab.model.User;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper public interface UserMapper {
 @Select("select * from sys_user where username=#{username}") User findByUsername(String username);
 @Select("select id,username,real_name,phone,role,status from sys_user order by id") List<User> list();
 @Select("select id,username,real_name,phone,role,status from sys_user where id=#{id}") User getById(@Param("id") Long id);
 @Update("update sys_user set role=#{role},status=#{status},real_name=#{realName},phone=#{phone} where id=#{id}") int update(User user);
 @Select("select count(*) from sys_user where role='SYSTEM_ADMIN' and status=1") int activeSystemAdmins();
 @Select("select count(*) from sys_user where role='SYSTEM_ADMIN'") int systemAdminCount();
 @Select("select count(*) from lab_admin_assignment where user_id=#{userId}") int assignmentCount(@Param("userId") Long userId);
}
