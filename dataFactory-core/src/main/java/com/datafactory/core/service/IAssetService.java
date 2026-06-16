package com.datafactory.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.asset.AssetCreateRequest;
import com.datafactory.common.model.dto.asset.AssetDirectoryCreateRequest;
import com.datafactory.common.model.dto.asset.AssetDirectoryUpdateRequest;
import com.datafactory.common.model.dto.asset.AssetUpdateRequest;
import com.datafactory.common.model.vo.asset.AssetDetailVo;
import com.datafactory.common.model.vo.asset.AssetDirectoryVo;
import com.datafactory.common.model.vo.asset.AssetListVo;

import java.util.List;

/**
 * 数据资产服务接口
 *
 * 提供资产目录树的查询与 CRUD，以及资产的增删改查、状态流转和批量操作。
 */
public interface IAssetService {

    // ==================== 资产目录 ====================

    /**
     * 查询资产目录树
     *
     * 获取资产目录的多层级树形结构（仅包含目录节点，不含资产）。
     *
     * @return 目录树列表（顶级节点数组）
     */
    List<AssetDirectoryVo> getDirectoryTree();

    /**
     * 新增资产目录
     *
     * @param request 新增目录请求参数
     * @return 创建的目录信息
     */
    AssetDirectoryVo createDirectory(AssetDirectoryCreateRequest request);

    /**
     * 编辑资产目录
     *
     * @param id      目录ID
     * @param request 编辑目录请求参数
     */
    void updateDirectory(Long id, AssetDirectoryUpdateRequest request);

    /**
     * 删除资产目录
     *
     * 仅能删除无子目录且无关联资产的目录。
     *
     * @param id 目录ID
     */
    void deleteDirectory(Long id);

    // ==================== 资产 CRUD ====================

    /**
     * 分页查询资产列表
     *
     * 排序规则：状态优先（未发布→已发布→已停用），再按更新时间倒序。
     * 选择某个目录时查询该目录及其所有后代目录下的资产，每资产只显示一次。
     *
     * @param pageNum     页码，默认 1
     * @param pageSize    每页条数，默认 20
     * @param keyword     关键词，模糊匹配中文名称、英文名称
     * @param status      状态筛选，多值用逗号分隔
     * @param directoryId 目录ID筛选
     * @return 分页结果
     */
    Page<AssetListVo> getAssetList(Integer pageNum, Integer pageSize, String keyword,
                                   String status, Long directoryId);

    /**
     * 查询资产详情
     *
     * @param id 资产ID
     * @return 资产详情（含字段定义和关联目录）
     */
    AssetDetailVo getAssetDetail(Long id);

    /**
     * 新增数据资产
     *
     * assetName 和 englishName 全局唯一，创建后状态为 DRAFT。
     *
     * @param request 新增资产请求参数
     * @return 创建的资产详情
     */
    AssetDetailVo createAsset(AssetCreateRequest request);

    /**
     * 编辑数据资产
     *
     * 仅未发布(0)和已停用(2)状态可编辑。
     *
     * @param id      资产ID
     * @param request 编辑资产请求参数
     */
    void updateAsset(Long id, AssetUpdateRequest request);

    /**
     * 删除数据资产
     *
     * 仅 DRAFT(0) 状态可删除。
     *
     * @param id 资产ID
     */
    void deleteAsset(Long id);

    // ==================== 状态流转 ====================

    /**
     * 发布数据资产
     *
     * 将未发布(0)或已停用(2)状态的资产发布为已发布(1)。
     *
     * @param id 资产ID
     */
    void publishAsset(Long id);

    /**
     * 停用数据资产
     *
     * 将已发布(1)状态的资产变更为已停用(2)。
     *
     * @param id 资产ID
     */
    void disableAsset(Long id);

    // ==================== 批量操作 ====================

    /**
     * 批量发布数据资产
     *
     * 所选资产须全部为未发布(0)或已停用(2)，不能包含已发布(1)。
     *
     * @param request 批量操作请求
     */
    void batchPublish(BatchIdsRequest request);

    /**
     * 批量停用数据资产
     *
     * 所选资产须全部为已发布(1)，不能包含未发布(0)或已停用(2)。
     *
     * @param request 批量操作请求
     */
    void batchDisable(BatchIdsRequest request);
}
