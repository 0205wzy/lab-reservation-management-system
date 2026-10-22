package com.zjnu.lab.model;
import lombok.Data;
import java.time.*;
import jakarta.validation.constraints.*;
@Data public class Reservation {
 private Long id; private Long applicantId; private String applicantName; private Long labId; private String labName;
 @NotNull private LocalDate reserveDate; @NotNull private LocalTime startTime; @NotNull private LocalTime endTime;
 @NotBlank @Size(max=255) private String purpose; @NotNull @Min(1) private Integer participants;
 @Size(max=500) private String remark; private String status; private String rejectReason; private Long auditorId;
 private LocalDateTime auditTime; private LocalDateTime createdAt;
}
