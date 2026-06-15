package com.datafactory.common.model.dto.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 脚本调试请求参数 DTO
 */
@Data
@Schema(description = "脚本调试请求参数")
public class ScriptDebugRequest {

    @Schema(description = "参数键值对，Key 为参数名，Value 为测试值", example = "{\"企业唯一id\": \"1001\"}")
    private Map<String, Object> params;
}
