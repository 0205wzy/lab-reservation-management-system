package com.zjnu.lab.mapper;
import com.zjnu.lab.model.RepairOrder;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper public interface RepairMapper {
 @Select("<script>select r.*,d.name device_name,u.real_name reporter_name,h.real_name handler_name from repair_order r join device d on d.id=r.device_id join sys_user u on u.id=r.reporter_id left join sys_user h on h.id=r.handler_id where 1=1 <if test='userId!=null'>and r.reporter_id=#{userId}</if><if test='status!=null and status!=\"\"'>and r.status=#{status}</if><if test='labIds!=null'>and d.lab_id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> order by r.id desc</script>") List<RepairOrder> list(@Param("userId")Long userId,@Param("status")String status,@Param("labIds")List<Long> labIds);
 @Select("select count(*) from repair_order where device_id=#{deviceId} and status in ('PENDING','PROCESSING')") int active(Long deviceId);
 @Select("select status from repair_order where device_id=#{deviceId} and status in ('PENDING','PROCESSING') order by id desc limit 1") String activeStatus(Long deviceId);
 @Select("select count(*) from repair_order where device_id=#{deviceId} and reporter_id=#{reporterId} and status in ('PENDING','PROCESSING')") int activeByReporter(@Param("deviceId") Long deviceId,@Param("reporterId") Long reporterId);
 @Insert("insert into repair_order(device_id,reporter_id,fault_desc,severity,remark,status,created_at) values(#{deviceId},#{reporterId},#{faultDesc},#{severity},#{remark},'PENDING',current_timestamp)") @Options(useGeneratedKeys=true,keyProperty="id") int insert(RepairOrder r);
 @Update("update repair_order set handler_id=#{handlerId},status=#{status},result=#{result},completed_at=case when #{status}='COMPLETED' then current_timestamp else completed_at end where id=#{id} and status=#{fromStatus}") int handle(@Param("id")Long id,@Param("handlerId")Long handlerId,@Param("status")String status,@Param("fromStatus")String fromStatus,@Param("result")String result);
 @Select("select device_id from repair_order where id=#{id}") Long deviceId(Long id);
 @Select("select d.lab_id from repair_order r join device d on d.id=r.device_id where r.id=#{id}") Long labId(Long id);
}
