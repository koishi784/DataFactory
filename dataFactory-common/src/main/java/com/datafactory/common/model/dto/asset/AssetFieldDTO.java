package com.datafactory.common.model.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 资产字段定义 DTO
 */
@Data
@Schema(description = "资产字段定义")
public class AssetFieldDTO {

    @Schema(description = "字段英文名称（仅支持英文大小写、数字及下划线，英文开头）", example = "firmname", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字段英文名称不能为空")
    @Size(max = 100, message = "字段英文名称最大 100 字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "字段英文名称仅支持英文大小写、数字及下划线，须以英文字母开头")
    private String englishFieldName;

    @Schema(description = "字段中文名称（仅支持中文及英文大小写）", example = "企业名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字段中文名称不能为空")
    @Size(max = 100, message = "字段中文名称最大 100 字符")
    @Pattern(regexp = "^[a-zA-Z\u4e00-\u9fa5]+$", message = "字段中文名称仅支持中文及英文大小写")
    private String chineseFieldName;

    @Schema(description = "字段说明", example = "企业注册名称")
    @Size(max = 200, message = "字段说明最大 200 字符")
    private String description;

    @Schema(description = "关联数据标准ID", example = "1")
    private Long standardId;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;
}
