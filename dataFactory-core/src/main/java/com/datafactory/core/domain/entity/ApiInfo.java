package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 注册接口实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_info")
public class ApiInfo extends BaseEntity {

    /**
     * 接口名称（最大 100 字符，全局唯一，不允许空格）
     */
    private String apiName;

    /**
     * 接口说明（最大 1000 字符）
     */
    private String apiDescription;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 接口来源
     */
    private String source;

    /**
     * 协议类型：HTTP / HTTPS
     */
    private String protocol;

    /**
     * 请求方法：GET / POST
     */
    private String method;

    /**
     * 接口URL路径（全局唯一）
     */
    private String url;

    /**
     * 超时时间（毫秒，范围 1~1800000，默认 30000 = 30 秒）
     */
    private Integer timeout;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 状态：0=未发布，1=已发布，2=已停用
     */
    private Integer status;

    /**
     * 当前版本号
     */
    private String version;

    /**
     * 响应示例
     */
    private String responseExample;

    /**
     * 备注
     */
    private String remark;
}
