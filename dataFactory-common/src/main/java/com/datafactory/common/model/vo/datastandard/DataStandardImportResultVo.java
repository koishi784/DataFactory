package com.datafactory.common.model.vo.datastandard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据标准导入结果 VO
 *
 * 包含导入统计信息和失败详情列表。
 */
@Data
@Schema(description = "数据标准导入结果")
public class DataStandardImportResultVo {

    /**
     * 导入文件中的总数据行数（不含表头）
     */
    @Schema(description = "导入文件中的总数据行数（不含表头）")
    private Integer totalCount;

    /**
     * 导入成功条数
     */
    @Schema(description = "导入成功条数")
    private Integer successCount;

    /**
     * 导入失败条数
     */
    @Schema(description = "导入失败条数")
    private Integer failCount;

    /**
     * 失败详情列表
     */
    @Schema(description = "失败详情列表")
    private List<FailDetail> failDetails;

    /**
     * 失败详情内部类
     */
    @Data
    @Schema(description = "导入失败详情")
    public static class FailDetail {

        /**
         * 行号（对应 Excel 行号，从 2 开始）
         */
        @Schema(description = "行号（对应 Excel 行号，从 2 开始）")
        private Integer rowIndex;

        /**
         * 失败原因
         */
        @Schema(description = "失败原因")
        private String reason;

        public FailDetail() {}

        public FailDetail(Integer rowIndex, String reason) {
            this.rowIndex = rowIndex;
            this.reason = reason;
        }
    }

    public DataStandardImportResultVo() {
        this.totalCount = 0;
        this.successCount = 0;
        this.failCount = 0;
        this.failDetails = new ArrayList<>();
    }
}
