package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统文件记录实体类
 *
 * 记录上传文件的元数据信息，文件内容存储在磁盘指定路径下。
 */
@Data
@TableName("sys_file")
public class SysFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储路径（相对路径）
     */
    private String storedPath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
