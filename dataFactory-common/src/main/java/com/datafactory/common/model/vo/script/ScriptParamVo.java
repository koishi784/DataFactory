package com.datafactory.common.model.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 脚本参数 VO
 */
@Data
@Schema(description = "脚本参数信息")
public class ScriptParamVo {

    @Schema(description = "参数ID")
    private Long id;

    @Schema(description = "参数名称")
    private String paramName;

    @Schema(description = "参数数据类型")
    private String paramType;

    @Schema(description = "参数描述")
    private String description;
}
