package com.zjnu.lab.controller;

import com.zjnu.lab.common.ApiResponse;
import com.zjnu.lab.dto.AiAnswer;
import com.zjnu.lab.dto.AiQuestion;
import com.zjnu.lab.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAssistantController {
    private final AiAssistantService assistant;

    @PostMapping("/ask")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "咨询AI实验室助手", description = "仅登录学生可提问。回答仅提供咨询指导，不会执行预约或修改数据。")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "回答成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "问题为空或超过500字"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "角色无权访问")
    })
    public ApiResponse<AiAnswer> ask(@Valid @RequestBody AiQuestion question) {
        return ApiResponse.ok(assistant.answer(question.question()));
    }
}
