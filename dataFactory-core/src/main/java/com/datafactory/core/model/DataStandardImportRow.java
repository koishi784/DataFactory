package com.datafactory.core.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 数据标准导入 Excel 行模型
 *
 * 映射导入模板的各列（中文表头），用于 EasyExcel 读取。
 */
@Data
public class DataStandardImportRow {

    /**
     * 中文名称（必填）
     */
    @ExcelProperty("中文名称")
    private String name;

    /**
     * 英文名称（必填）
     */
    @ExcelProperty("英文名称")
    private String englishName;

    /**
     * 数据类型（必填）：String / Int / Float / Enum
     */
    @ExcelProperty("数据类型")
    private String dataType;

    /**
     * 数据长度（仅 String 类型可填）
     */
    @ExcelProperty("数据长度")
    private Integer length;

    /**
     * 数据精度（仅 Float 类型可填）
     */
    @ExcelProperty("数据精度")
    private Integer precision;

    /**
     * 默认值
     */
    @ExcelProperty("默认值")
    private String defaultValue;

    /**
     * 取值范围最小值（仅 Int / Float 类型可填）
     */
    @ExcelProperty("取值范围最小值")
    private String rangeMin;

    /**
     * 取值范围最大值（仅 Int / Float 类型可填）
     */
    @ExcelProperty("取值范围最大值")
    private String rangeMax;

    /**
     * 引用码表编号（仅 Enum 类型可填）
     */
    @ExcelProperty("引用码表编号")
    private String enumRange;

    /**
     * 来源机构（必填）
     */
    @ExcelProperty("来源机构")
    private String sourceOrganization;

    /**
     * 是否可为空：0=可为空 / 1=不可为空
     */
    @ExcelProperty("是否可为空")
    private Integer nullable;

    /**
     * 标准说明
     */
    @ExcelProperty("标准说明")
    private String description;

    /** 对应的 Excel 行号（从 2 开始，表头为第 1 行） */
    private Integer rowIndex;
}
