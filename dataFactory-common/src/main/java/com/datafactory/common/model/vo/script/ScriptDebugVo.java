package com.datafactory.common.model.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 脚本调试结果 VO
 */
@Data
@Schema(description = "脚本调试结果")
public class ScriptDebugVo {

    @Schema(description = "执行是否成功")
    private Boolean success;

    @Schema(description = "执行耗时（毫秒）")
    private Long executeTime;

    @Schema(description = "执行结果")
    private String result;

    @Schema(description = "错误信息（失败时返回）")
    private String errorMessage;
}
