package com.zjnu.lab.controller;

import com.zjnu.lab.common.ApiResponse;
import com.zjnu.lab.common.BusinessException;
import com.zjnu.lab.mapper.ReportMapper;
import com.zjnu.lab.service.LabScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/manage/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportMapper reports;
    private final LabScopeService scope;

    @GetMapping
    public ApiResponse<?> report(@RequestParam(required = false) LocalDate fromDate,
                                 @RequestParam(required = false) LocalDate toDate,
                                 @RequestParam(required = false) Long labId) {
        validateRange(fromDate, toDate);
        validateLabScope(labId);
        var labIds = scope.scope();
        return ApiResponse.ok(Map.of(
                "summary", reports.summary(fromDate, toDate, labId, labIds),
                "daily", reports.daily(fromDate, toDate, labId, labIds),
                "statuses", reports.statuses(fromDate, toDate, labId, labIds),
                "labs", reports.byLab(fromDate, toDate, labId, labIds)));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) LocalDate fromDate,
                                         @RequestParam(required = false) LocalDate toDate,
                                         @RequestParam(required = false) Long labId) throws IOException {
        validateRange(fromDate, toDate);
        validateLabScope(labId);
        List<Map<String, Object>> rows = reports.exportReservations(fromDate, toDate, labId, scope.scope());
        String[] headers = {"预约编号", "实验室", "申请人", "预约日期", "开始时间", "结束时间", "人数", "用途", "状态", "拒绝原因", "提交时间"};
        String[] keys = {"id", "labName", "applicantName", "reserveDate", "startTime", "endTime",
                "participants", "purpose", "status", "rejectReason", "createdAt"};
        Map<String, String> statusLabels = Map.of(
                "PENDING", "待审核", "APPROVED", "已通过", "REJECTED", "已拒绝",
                "CANCELLED", "已取消", "COMPLETED", "已完成");
        StringBuilder sheet = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>
                """);
        appendRow(sheet, 1, List.of(headers));
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Map<String, Object> values = rows.get(rowIndex);
            Object[] cells = new Object[keys.length];
            for (int col = 0; col < keys.length; col++) {
                Object value = values.get(keys[col]);
                if ("status".equals(keys[col]) && value != null) value = statusLabels.getOrDefault(value.toString(), value.toString());
                cells[col] = value == null ? "" : value.toString();
            }
            appendRow(sheet, rowIndex + 2, List.of(cells));
        }
        sheet.append("</sheetData></worksheet>");
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            addEntry(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                      <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    </Types>
                    """);
            addEntry(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>
                    """);
            addEntry(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                      <sheets><sheet name="预约报表" sheetId="1" r:id="rId1"/></sheets>
                    </workbook>
                    """);
            addEntry(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                    </Relationships>
                    """);
            addEntry(zip, "xl/worksheets/sheet1.xml", sheet.toString());
            zip.finish();
            String filename = URLEncoder.encode("实验室预约报表.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .body(output.toByteArray());
        }
    }

    private void appendRow(StringBuilder xml, int rowNumber, List<?> values) {
        xml.append("<row r=\"").append(rowNumber).append("\">");
        for (int col = 0; col < values.size(); col++) {
            xml.append("<c r=\"").append(columnName(col + 1)).append(rowNumber).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                    .append(escapeXml(values.get(col) == null ? "" : values.get(col).toString()))
                    .append("</t></is></c>");
        }
        xml.append("</row>");
    }

    private String columnName(int number) {
        StringBuilder name = new StringBuilder();
        while (number > 0) {
            int remainder = (number - 1) % 26;
            name.insert(0, (char) ('A' + remainder));
            number = (number - 1) / 26;
        }
        return name.toString();
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private void addEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
    }

    private void validateLabScope(Long labId) {
        if (labId != null) scope.requireAccess(labId);
    }
}
