package com.zjnu.lab.model;
import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
@Data public class RepairOrder {
 private Long id; @NotNull private Long deviceId; private String deviceName; private Long reporterId; private String reporterName;
 private Long handlerId; private String handlerName; @NotBlank @Size(max=500) private String faultDesc;
 @NotBlank private String severity; @Size(max=500) private String remark; private String status; private String result;
 private LocalDateTime createdAt; private LocalDateTime completedAt;
}
