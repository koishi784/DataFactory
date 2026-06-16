package com.datafactory.common.model.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增数据资产请求参数
 */
@Data
@Schema(description = "新增数据资产请求参数")
public class AssetCreateRequest {

    @Schema(description = "中文名称（全局唯一，仅支持中英文）", example = "企业基本信息表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "中文名称不能为空")
    @Size(max = 50, message = "中文名称最大 50 字符")
    @Pattern(regexp = "^[a-zA-Z\u4e00-\u9fa5]+$", message = "中文名称仅支持中文及英文大小写")
    private String assetName;

    @Schema(description = "英文名称（全局唯一，仅支持英文大小写、数字及下划线，英文开头）", example = "CommercialRegInfo", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "英文名称不能为空")
    @Size(max = 100, message = "英文名称最大 100 字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "英文名称仅支持英文大小写、数字及下划线，须以英文字母开头")
    private String englishName;

    @Schema(description = "数据资产表描述", example = "企业工商注册基本信息")
    @Size(max = 500, message = "描述最大 500 字符")
    private String description;

    @Schema(description = "所属目录ID列表，必须为叶子节点目录", example = "[3, 5]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "所属目录不能为空")
    private List<Long> directoryIds;

    @Schema(description = "字段定义列表")
    @Valid
    private List<AssetFieldDTO> fields;
}
