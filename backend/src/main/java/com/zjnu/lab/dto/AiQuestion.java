package com.zjnu.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI实验室助手提问")
public record AiQuestion(
        @Schema(description = "问题内容", example = "实验室预约需要注意什么？")
        @NotBlank(message = "问题不能为空")
        @Size(max = 500, message = "问题最多500个字符")
        String question
) {
}
