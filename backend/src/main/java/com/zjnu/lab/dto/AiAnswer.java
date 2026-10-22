package com.zjnu.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI实验室助手回答")
public record AiAnswer(
        @Schema(description = "回答内容")
        String answer,
        @Schema(description = "回答来源：AI为在线模型，LOCAL为本地知识")
        String source
) {
}
