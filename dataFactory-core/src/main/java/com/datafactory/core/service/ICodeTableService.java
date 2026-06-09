package com.datafactory.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.codetable.CodeItemCreateRequest;
import com.datafactory.common.model.dto.codetable.CodeItemUpdateRequest;
import com.datafactory.common.model.dto.codetable.CodeTableCreateRequest;
import com.datafactory.common.model.dto.codetable.CodeTableUpdateRequest;
import com.datafactory.common.model.vo.codetable.CodeItemVo;
import com.datafactory.common.model.vo.codetable.CodeTableDetailVo;
import com.datafactory.common.model.vo.codetable.CodeTableListVo;
import com.datafactory.core.domain.entity.CodeTable;

import java.util.List;

/**
 * 码表管理服务接口
 *
 * 提供码表的 CRUD、状态管理（发布/停用/删除）、批量操作，以及码值的增删改查等业务方法。
 */
public interface ICodeTableService {

    /**
     * 分页查询码表列表
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，匹配码表名称、码表编号
     * @param status   状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @return 分页结果
     */
    Page<CodeTableListVo> getCodeTableList(Integer pageNum, Integer pageSize, String keyword, String status);

    /**
     * 查询码表详情（含码值列表）
     *
     * @param id 码表ID
     * @return 码表详情
     */
    CodeTableDetailVo getCodeTableDetail(Long id);

    /**
     * 查询码值列表
     *
     * @param tableId 码表ID
     * @return 码值列表
     */
    List<CodeItemVo> getCodeItems(Long tableId);

    /**
     * 新增码表
     *
     * 码表编号由系统自动生成（格式 MZB + 5 位数字），创建后状态为 DRAFT(0)。
     * 支持同时传入初始码值列表。
     *
     * @param request 新增码表请求参数
     * @return 创建的码表详情
     */
    CodeTableDetailVo createCodeTable(CodeTableCreateRequest request);

    /**
     * 编辑码表
     *
     * 仅未发布(0)和已停用(2)状态可编辑。码表编号不可修改。
     *
     * @param id      码表ID
     * @param request 编辑码表请求参数
     */
    void updateCodeTable(Long id, CodeTableUpdateRequest request);

    /**
     * 新增码值
     *
     * @param tableId 码表ID
     * @param request 新增码值请求参数
     * @return 创建的码值
     */
    CodeItemVo createCodeItem(Long tableId, CodeItemCreateRequest request);

    /**
     * 更新码值
     *
     * @param tableId 码表ID
     * @param itemId  码值ID
     * @param request 更新码值请求参数
     */
    void updateCodeItem(Long tableId, Long itemId, CodeItemUpdateRequest request);

    /**
     * 删除码值
     *
     * @param tableId 码表ID
     * @param itemId  码值ID
     */
    void deleteCodeItem(Long tableId, Long itemId);

    /**
     * 发布码表
     *
     * 将未发布(0)或已停用(2)状态的标准发布为已发布(1)。
     *
     * @param id 码表ID
     */
    void publishCodeTable(Long id);

    /**
     * 停用码表
     *
     * 将已发布(1)状态变更为已停用(2)。
     *
     * @param id 码表ID
     */
    void disableCodeTable(Long id);

    /**
     * 删除码表
     *
     * 仅可删除 DRAFT(0) 状态的码表。
     *
     * @param id 码表ID
     */
    void deleteCodeTable(Long id);

    /**
     * 批量发布码表
     *
     * @param request 批量操作请求
     */
    void batchPublish(BatchIdsRequest request);

    /**
     * 批量停用码表
     *
     * @param request 批量操作请求
     */
    void batchDisable(BatchIdsRequest request);

    /**
     * 根据编码查询码表（供其他模块调用）
     *
     * @param tableCode 码表编号
     * @return 码表实体，不存在返回 null
     */
    CodeTable getByTableCode(String tableCode);
}
