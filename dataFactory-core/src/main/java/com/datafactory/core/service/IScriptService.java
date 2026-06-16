package com.datafactory.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.script.ScriptBatchCategoryRequest;
import com.datafactory.common.model.dto.script.ScriptCategoryCreateRequest;
import com.datafactory.common.model.dto.script.ScriptCategoryUpdateRequest;
import com.datafactory.common.model.dto.script.ScriptCreateRequest;
import com.datafactory.common.model.dto.script.ScriptDebugRequest;
import com.datafactory.common.model.dto.script.ScriptUpdateRequest;
import com.datafactory.common.model.vo.script.ScriptCategoryVo;
import com.datafactory.common.model.vo.script.ScriptDebugVo;
import com.datafactory.common.model.vo.script.ScriptDetailVo;
import com.datafactory.common.model.vo.script.ScriptListVo;

import java.util.List;

/**
 * 脚本管理服务接口
 *
 * 提供脚本分类树的查询与 CRUD，以及脚本的增删改查、在线调试、状态流转和批量操作。
 */
public interface IScriptService {

    // ==================== 分类管理 ====================

    /**
     * 查询脚本分类树
     *
     * 获取全量脚本分类的多层级树形结构。
     *
     * @return 分类树列表（顶级节点数组）
     */
    List<ScriptCategoryVo> getCategoryTree();

    /**
     * 新增脚本分类
     *
     * 同一父级分类下子分类名称不可重复。
     *
     * @param request 新增分类请求参数
     * @return 创建的分类信息
     */
    ScriptCategoryVo createCategory(ScriptCategoryCreateRequest request);

    /**
     * 编辑脚本分类
     *
     * @param id      分类ID
     * @param request 编辑分类请求参数
     */
    void updateCategory(Long id, ScriptCategoryUpdateRequest request);

    /**
     * 删除脚本分类
     *
     * 仅能删除无子分类且无关联脚本的分类。
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    // ==================== 脚本 CRUD ====================

    /**
     * 分页查询脚本列表
     *
     * 排序规则：优先级一按状态（0→1→2），优先级二按更新时间倒序。
     * 选择某个分类时查询该分类及其所有后代分类下的脚本。
     *
     * @param pageNum   页码，默认 1
     * @param pageSize  每页条数，默认 20
     * @param keyword   关键词，模糊匹配脚本名称、说明
     * @param status    状态筛选，多值用逗号分隔
     * @param categoryId 分类ID筛选
     * @return 分页结果
     */
    Page<ScriptListVo> getScriptList(Integer pageNum, Integer pageSize, String keyword,
                                     String status, Long categoryId);

    /**
     * 查询脚本详情
     *
     * @param id 脚本ID
     * @return 脚本详情（含输入/输出参数）
     */
    ScriptDetailVo getScriptDetail(Long id);

    /**
     * 新增脚本
     *
     * scriptName 全局唯一，创建后状态为 DRAFT。
     *
     * @param request 新增脚本请求参数
     * @return 创建的脚本详情
     */
    ScriptDetailVo createScript(ScriptCreateRequest request);

    /**
     * 编辑脚本
     *
     * 仅未发布(0)和已停用(2)状态可编辑。
     *
     * @param id      脚本ID
     * @param request 编辑脚本请求参数
     */
    void updateScript(Long id, ScriptUpdateRequest request);

    /**
     * 删除脚本
     *
     * 仅 DRAFT(0) 状态可删除。
     *
     * @param id 脚本ID
     */
    void deleteScript(Long id);

    // ==================== 状态流转 ====================

    /**
     * 发布脚本
     *
     * 将未发布(0)或已停用(2)状态的脚本发布为已发布(1)。
     *
     * @param id 脚本ID
     */
    void publishScript(Long id);

    /**
     * 停用脚本
     *
     * 将已发布(1)状态的脚本变更为已停用(2)。
     *
     * @param id 脚本ID
     */
    void disableScript(Long id);

    // ==================== 调试 ====================

    /**
     * 在线调试脚本
     *
     * 读取脚本文件内容并通过对应的执行器（Groovy / Python）执行。
     * 不限制脚本状态。
     *
     * @param id      脚本ID
     * @param request 调试请求参数（含参数键值对）
     * @return 调试执行结果
     */
    ScriptDebugVo debugScript(Long id, ScriptDebugRequest request);

    // ==================== 批量操作 ====================

    /**
     * 批量发布脚本
     *
     * 所选脚本须全部为未发布(0)或已停用(2)，不能包含已发布(1)。
     *
     * @param request 批量操作请求
     */
    void batchPublish(BatchIdsRequest request);

    /**
     * 批量停用脚本
     *
     * 所选脚本须全部为已发布(1)，不能包含未发布(0)或已停用(2)。
     *
     * @param request 批量操作请求
     */
    void batchDisable(BatchIdsRequest request);

    /**
     * 批量修改脚本分类
     *
     * 所选脚本须全部为未发布(0)或已停用(2)，不能包含已发布(1)。
     *
     * @param request 批量分类请求
     */
    void batchCategory(ScriptBatchCategoryRequest request);
}
