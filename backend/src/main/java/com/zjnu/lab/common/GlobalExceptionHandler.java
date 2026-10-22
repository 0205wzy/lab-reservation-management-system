package com.zjnu.lab.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> business(BusinessException e) { return ApiResponse.fail(400, e.getMessage()); }
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> validation(Exception e) {
        var errors = e instanceof MethodArgumentNotValidException ex
                ? ex.getBindingResult().getFieldErrors()
                : ((BindException) e).getBindingResult().getFieldErrors();
        if (!errors.isEmpty()) {
            FieldError error = errors.get(0);
            String message = switch (error.getField()) {
                case "labId" -> "请选择实验室";
                case "reserveDate" -> "请选择预约日期";
                case "startTime" -> "请选择开始时间";
                case "endTime" -> "请选择结束时间";
                case "purpose" -> "请填写使用用途";
                case "participants" -> "参与人数必须至少为1人";
                case "deviceId" -> "请选择故障设备";
                case "faultDesc" -> "请填写故障现象";
                case "severity" -> "请选择影响程度";
                case "question" -> error.getCode().equals("NotBlank")
                        ? "问题不能为空"
                        : "问题最多500个字符";
                default -> "请求参数不正确";
            };
            return ApiResponse.fail(400, message);
        }
        return ApiResponse.fail(400, "请求参数不正确");
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> error(Exception e) { return ApiResponse.fail(500, e.getMessage()); }
}
