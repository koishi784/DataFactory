package com.datafactory.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.datastandard.DataStandardCreateRequest;
import com.datafactory.common.model.dto.datastandard.DataStandardUpdateRequest;
import com.datafactory.common.model.vo.datastandard.DataStandardDetailVo;
import com.datafactory.common.model.vo.datastandard.DataStandardImportResultVo;
import com.datafactory.common.model.vo.datastandard.DataStandardListVo;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 数据标准服务接口
 *
 * 提供数据标准的 CRUD、状态管理（发布/停用/删除）、批量操作等业务方法
 */
public interface IDataStandardService {

    /**
     * 分页查询数据标准列表
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，模糊匹配中文名称、英文名称、标准编号
     * @param status   状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @param dataType 数据类型筛选：String / Int / Float / Enum
     * @return 分页结果
     */
    Page<DataStandardListVo> getStandardList(Integer pageNum, Integer pageSize, String keyword,
                                              String status, String dataType);

    /**
     * 查询数据标准详情
     *
     * @param id 标准ID
     * @return 数据标准详情
     */
    DataStandardDetailVo getStandardDetail(Long id);

    /**
     * 新增数据标准
     *
     * 标准编号由系统自动生成（格式 BZ + 5 位数字），创建后状态为 DRAFT(0)。
     *
     * @param request 新增数据标准请求参数
     * @return 创建的数据标准详情
     */
    DataStandardDetailVo createStandard(DataStandardCreateRequest request);

    /**
     * 编辑数据标准
     *
     * 仅未发布(0)和已停用(2)状态可编辑。标准编号不可修改。
     *
     * @param id      标准ID
     * @param request 编辑数据标准请求参数
     */
    void updateStandard(Long id, DataStandardUpdateRequest request);

    /**
     * 发布数据标准
     *
     * 将未发布(0)或已停用(2)状态的标准发布为已发布(1)。
     *
     * @param id 标准ID
     */
    void publishStandard(Long id);

    /**
     * 停用数据标准
     *
     * 将已发布(1)状态的标准变更为已停用(2)。
     *
     * @param id 标准ID
     */
    void disableStandard(Long id);

    /**
     * 删除数据标准
     *
     * 仅可删除 DRAFT(0) 状态的标准。
     *
     * @param id 标准ID
     */
    void deleteStandard(Long id);

    /**
     * 批量发布数据标准
     *
     * @param request 批量操作请求
     */
    void batchPublish(BatchIdsRequest request);

    /**
     * 批量停用数据标准
     *
     * @param request 批量操作请求
     */
    void batchDisable(BatchIdsRequest request);

    /**
     * 下载导入模板
     *
     * 生成包含表头行的 Excel 模板文件，供用户填写数据标准信息后批量导入。
     *
     * @param response HTTP 响应，用于输出文件流
     * @throws IOException 文件写入异常
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * 批量导入数据标准
     *
     * 通过上传 Excel 文件批量导入数据标准，按照六步校验规则进行校验和过滤。
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果（包含统计信息和失败详情）
     * @throws IOException 文件读取异常
     */
    DataStandardImportResultVo importStandards(MultipartFile file) throws IOException;
}
