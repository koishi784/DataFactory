package com.datafactory.common.model.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传结果")
public class FileUploadVo {

    @Schema(description = "文件ID", example = "1850123456789012345")
    private Long fileId;

    @Schema(description = "原始文件名", example = "清洗脚本.py")
    private String fileName;

    @Schema(description = "文件访问URL", example = "/api/v1/common/download/1")
    private String fileUrl;

    @Schema(description = "文件大小（字节）", example = "2048")
    private Long fileSize;
}
