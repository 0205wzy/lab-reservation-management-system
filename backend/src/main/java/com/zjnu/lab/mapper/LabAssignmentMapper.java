package com.zjnu.lab.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabAssignmentMapper {
    @Select("select lab_id from lab_admin_assignment where user_id=#{userId} order by lab_id")
    List<Long> findByUser(@Param("userId") Long userId);

    @Select("select a.user_id userId,a.lab_id labId,l.name labName from lab_admin_assignment a join lab l on l.id=a.lab_id order by a.user_id,a.lab_id")
    List<Assignment> list();

    @Delete("delete from lab_admin_assignment where user_id=#{userId}")
    int deleteByUser(@Param("userId") Long userId);

    @Insert("insert into lab_admin_assignment(user_id,lab_id) values(#{userId},#{labId})")
    int insert(@Param("userId") Long userId, @Param("labId") Long labId);

    record Assignment(Long userId, Long labId, String labName) {}
}
