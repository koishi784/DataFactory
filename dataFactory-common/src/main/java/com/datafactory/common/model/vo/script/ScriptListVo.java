package com.datafactory.common.model.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 脚本列表项 VO
 */
@Data
@Schema(description = "脚本列表项")
public class ScriptListVo {

    @Schema(description = "脚本ID")
    private Long id;

    @Schema(description = "脚本名称")
    private String scriptName;

    @Schema(description = "脚本类型：GROOVY / PYTHON")
    private String scriptType;

    @Schema(description = "所属分类ID")
    private Long categoryId;

    @Schema(description = "所属分类名称")
    private String categoryName;

    @Schema(description = "脚本文件名")
    private String fileName;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "状态：0=未发布 / 1=已发布 / 2=已停用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
