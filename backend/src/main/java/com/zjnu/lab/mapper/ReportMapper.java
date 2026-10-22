package com.zjnu.lab.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    @Select("""
        <script>
        select count(*) total,
          sum(case when status='PENDING' then 1 else 0 end) pending,
          sum(case when status='APPROVED' then 1 else 0 end) approved,
          sum(case when status='COMPLETED' then 1 else 0 end) completed,
          sum(case when status='CANCELLED' then 1 else 0 end) cancelled
        from reservation where 1=1
        <if test='fromDate != null'>and reserve_date &gt;= #{fromDate}</if>
        <if test='toDate != null'>and reserve_date &lt;= #{toDate}</if>
        <if test='labId != null'>and lab_id=#{labId}</if>
        <if test='labIds != null'>and lab_id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>
        </script>
        """)
    Map<String, Object> summary(@Param("fromDate") LocalDate fromDate,
                                @Param("toDate") LocalDate toDate,
                                @Param("labId") Long labId,
                                @Param("labIds") Collection<Long> labIds);

    @Select("""
        <script>
        select reserve_date reportDate, count(*) amount
        from reservation where 1=1
        <if test='fromDate != null'>and reserve_date &gt;= #{fromDate}</if>
        <if test='toDate != null'>and reserve_date &lt;= #{toDate}</if>
        <if test='labId != null'>and lab_id=#{labId}</if>
        <if test='labIds != null'>and lab_id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>
        group by reserve_date order by reserve_date
        </script>
        """)
    List<Map<String, Object>> daily(@Param("fromDate") LocalDate fromDate,
                                    @Param("toDate") LocalDate toDate,
                                    @Param("labId") Long labId,
                                    @Param("labIds") Collection<Long> labIds);

    @Select("""
        <script>
        select status name,count(*) amount
        from reservation where 1=1
        <if test='fromDate != null'>and reserve_date &gt;= #{fromDate}</if>
        <if test='toDate != null'>and reserve_date &lt;= #{toDate}</if>
        <if test='labId != null'>and lab_id=#{labId}</if>
        <if test='labIds != null'>and lab_id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>
        group by status order by status
        </script>
        """)
    List<Map<String, Object>> statuses(@Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate,
                                       @Param("labId") Long labId,
                                       @Param("labIds") Collection<Long> labIds);

    @Select("""
        <script>
        select l.name name,count(r.id) amount
        from lab l left join reservation r on r.lab_id=l.id
          <if test='fromDate != null'>and r.reserve_date &gt;= #{fromDate}</if>
          <if test='toDate != null'>and r.reserve_date &lt;= #{toDate}</if>
        where 1=1 <if test='labId != null'>and l.id=#{labId}</if>
        <if test='labIds != null'>and l.id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>
        group by l.id,l.name order by amount desc,l.name
        </script>
        """)
    List<Map<String, Object>> byLab(@Param("fromDate") LocalDate fromDate,
                                    @Param("toDate") LocalDate toDate,
                                    @Param("labId") Long labId,
                                    @Param("labIds") Collection<Long> labIds);

    @Select("""
        <script>
        select r.id,l.name labName,u.real_name applicantName,r.reserve_date reserveDate,
          r.start_time startTime,r.end_time endTime,r.participants,r.purpose,r.status,
          r.reject_reason rejectReason,r.created_at createdAt
        from reservation r join lab l on l.id=r.lab_id
          join sys_user u on u.id=r.applicant_id
        where 1=1
        <if test='fromDate != null'>and r.reserve_date &gt;= #{fromDate}</if>
        <if test='toDate != null'>and r.reserve_date &lt;= #{toDate}</if>
        <if test='labId != null'>and r.lab_id=#{labId}</if>
        <if test='labIds != null'>and r.lab_id in <foreach collection='labIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>
        order by r.reserve_date desc,r.start_time
        </script>
        """)
    List<Map<String, Object>> exportReservations(@Param("fromDate") LocalDate fromDate,
                                                @Param("toDate") LocalDate toDate,
                                                @Param("labId") Long labId,
                                                @Param("labIds") Collection<Long> labIds);
}
