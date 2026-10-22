package com.zjnu.lab.mapper;
import com.zjnu.lab.model.Device;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper public interface DeviceMapper {
 @Select("<script>select d.*,l.name lab_name from device d left join lab l on l.id=d.lab_id where 1=1 <if test='keyword!=null and keyword!=\"\"'>and (d.name like concat('%',#{keyword},'%') or d.device_code like concat('%',#{keyword},'%'))</if><if test='status!=null and status!=\"\"'>and d.status=#{status}</if><if test='labId!=null'>and d.lab_id=#{labId}</if><if test='labIds!=null'>and d.lab_id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if> order by d.id desc</script>") List<Device> list(@Param("keyword") String keyword,@Param("status") String status,@Param("labId") Long labId,@Param("labIds") List<Long> labIds);
 @Select("select d.*,l.name lab_name from device d left join lab l on l.id=d.lab_id where d.id=#{id}") Device get(Long id);
 @Insert("insert into device(device_code,name,category,model,lab_id,purchase_date,status,remark) values(#{deviceCode},#{name},#{category},#{model},#{labId},#{purchaseDate},#{status},#{remark})") @Options(useGeneratedKeys=true,keyProperty="id") int insert(Device d);
 @Update("update device set device_code=#{deviceCode},name=#{name},category=#{category},model=#{model},lab_id=#{labId},purchase_date=#{purchaseDate},status=#{status},remark=#{remark} where id=#{id}") int update(Device d);
 @Update("update device set status=#{status} where id=#{id}") int updateStatus(@Param("id") Long id,@Param("status") String status);
}
