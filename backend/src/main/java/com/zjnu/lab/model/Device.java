package com.zjnu.lab.model;
import lombok.Data;
import java.time.LocalDate;
@Data public class Device { private Long id; private String deviceCode; private String name; private String category; private String model; private Long labId; private String labName; private LocalDate purchaseDate; private String status; private String remark; }
