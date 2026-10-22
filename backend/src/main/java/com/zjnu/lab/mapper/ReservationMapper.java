package com.zjnu.lab.mapper;
import com.zjnu.lab.model.Reservation;
import org.apache.ibatis.annotations.*;
import java.time.*; import java.util.*;
@Mapper public interface ReservationMapper {
 @Select("<script>select r.*,u.real_name applicant_name,l.name lab_name from reservation r join sys_user u on u.id=r.applicant_id join lab l on l.id=r.lab_id where 1=1 <if test='userId!=null'>and r.applicant_id=#{userId}</if><if test='status!=null and status!=\"\"'>and r.status=#{status}</if><if test='labIds!=null'>and r.lab_id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> order by r.reserve_date desc,r.start_time desc</script>") List<Reservation> list(@Param("userId")Long userId,@Param("status")String status,@Param("labIds")List<Long> labIds);
 @Select("select * from reservation where id=#{id}") Reservation get(Long id);
 @Select("select reserve_date reserveDate,start_time startTime,end_time endTime,status from reservation where lab_id=#{labId} and reserve_date between #{from} and #{to} and status='APPROVED' order by reserve_date,start_time") List<Reservation> calendar(@Param("labId")Long labId,@Param("from")LocalDate from,@Param("to")LocalDate to);
 @Select("select start_time startTime,end_time endTime from reservation where lab_id=#{labId} and reserve_date=#{date} and status='APPROVED' order by start_time") List<Reservation> busyPeriods(@Param("labId")Long labId,@Param("date")LocalDate date);
 @Select("select count(*) from reservation where lab_id=#{labId} and reserve_date=#{date} and status='APPROVED' and start_time < #{end} and end_time > #{start} and (#{excludeId} is null or id!=#{excludeId})") int conflict(@Param("labId")Long labId,@Param("date")LocalDate date,@Param("start")LocalTime start,@Param("end")LocalTime end,@Param("excludeId")Long excludeId);
 @Select("select id from lab where id=#{labId} for update") Long lockLabForApproval(@Param("labId")Long labId);
 @Insert("insert into reservation(applicant_id,lab_id,reserve_date,start_time,end_time,purpose,participants,remark,status,created_at) values(#{applicantId},#{labId},#{reserveDate},#{startTime},#{endTime},#{purpose},#{participants},#{remark},'PENDING',current_timestamp)") @Options(useGeneratedKeys=true,keyProperty="id") int insert(Reservation r);
 @Update("update reservation set status=#{status},reject_reason=#{reason},auditor_id=#{auditorId},audit_time=current_timestamp where id=#{id} and status='PENDING'") int audit(@Param("id")Long id,@Param("status")String status,@Param("reason")String reason,@Param("auditorId")Long auditorId);
 @Update("update reservation set status='CANCELLED' where id=#{id}") int cancel(Long id);
 @Update("update reservation set status='COMPLETED' where status='APPROVED' and (reserve_date < current_date or (reserve_date=current_date and end_time <= current_time))") int completeDue();
}
