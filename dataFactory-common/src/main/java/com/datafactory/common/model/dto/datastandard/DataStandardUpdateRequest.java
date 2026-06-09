package com.datafactory.common.model.dto.datastandard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑数据标准请求参数
 *
 * 与新增请求参数相同，但 standardCode 不接受修改（忽略传入值）。
 */
@Data
@Schema(description = "编辑数据标准请求参数")
public class DataStandardUpdateRequest {

    @Schema(description = "中文名称", example = "企业类型更新", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "中文名称不能为空")
    @Size(max = 50, message = "中文名称最大 50 字符")
    @Pattern(regexp = "^[a-zA-Z\u4e00-\u9fa5]+$", message = "中文名称仅支持中文及英文大小写")
    private String name;

    @Schema(description = "英文名称", example = "firmTypeNew", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "英文名称不能为空")
    @Size(max = 100, message = "英文名称最大 100 字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "英文名称仅支持英文大小写、数字及下划线，须以英文字母开头")
    private String englishName;

    @Schema(description = "数据类型：String / Int / Float / Enum", example = "Enum", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "数据类型不能为空")
    @Pattern(regexp = "^(String|Int|Float|Enum)$", message = "数据类型仅支持 String / Int / Float / Enum")
    private String dataType;

    @Schema(description = "来源机构", example = "数宜信", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "来源机构不能为空")
    private String sourceOrganization;

    @Schema(description = "数据长度（仅 String 类型可填）", example = "50")
    private Integer length;

    @Schema(description = "精度（仅 Float 类型可填）", example = "2")
    private Integer precision;

    @Schema(description = "默认值", example = "0")
    private String defaultValue;

    @Schema(description = "取值范围最小值（仅 Int / Float 类型可填）", example = "1")
    private String rangeMin;

    @Schema(description = "取值范围最大值（仅 Int / Float 类型可填）", example = "999999")
    private String rangeMax;

    @Schema(description = "枚举范围，引用码表编码（仅 Enum 类型可填）", example = "FIRM_TYPE_CODE")
    private String enumRange;

    @Schema(description = "是否可为空：0=可为空 / 1=不可为空", example = "1")
    private Integer nullable;

    @Schema(description = "标准说明", example = "企业类型码表")
    @Size(max = 500, message = "标准说明最大 500 字符")
    private String description;
}
