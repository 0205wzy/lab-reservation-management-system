package com.zjnu.lab.mapper;
import com.zjnu.lab.model.Lab;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper public interface LabMapper {
 @Select("<script>select l.id,l.name,l.location,l.capacity,coalesce((select u.real_name from lab_admin_assignment a join sys_user u on u.id=a.user_id where a.lab_id=l.id),l.manager) manager,l.phone,l.description,l.open_time,l.close_time,l.open_status from lab l where (#{keyword} is null or l.name like concat('%',#{keyword},'%') or l.location like concat('%',#{keyword},'%')) <if test='labIds != null'>and l.id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> order by l.id desc</script>") List<Lab> list(@Param("keyword") String keyword,@Param("labIds") List<Long> labIds);
 @Select("select l.id,l.name,l.location,l.capacity,coalesce((select u.real_name from lab_admin_assignment a join sys_user u on u.id=a.user_id where a.lab_id=l.id),l.manager) manager,l.phone,l.description,l.open_time,l.close_time,l.open_status from lab l where l.id=#{id}") Lab get(Long id);
 @Select("<script>select count(*) from lab where id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>") int countByIds(@Param("ids") List<Long> ids);
 @Select("<script>select count(*) from lab_admin_assignment where lab_id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> and user_id&lt;&gt;#{userId}</script>") int assignedToOtherManagers(@Param("labIds") List<Long> labIds,@Param("userId") Long userId);
 @Insert("insert into lab(name,location,capacity,manager,phone,description,open_time,close_time,open_status) values(#{name},#{location},#{capacity},#{manager},#{phone},#{description},#{openTime},#{closeTime},#{openStatus})") @Options(useGeneratedKeys=true,keyProperty="id") int insert(Lab lab);
 @Update("update lab set name=#{name},location=#{location},capacity=#{capacity},manager=#{manager},phone=#{phone},description=#{description},open_time=#{openTime},close_time=#{closeTime},open_status=#{openStatus} where id=#{id}") int update(Lab lab);
 @Update("update lab set open_status=0 where id=#{id}") int disable(Long id);
}
