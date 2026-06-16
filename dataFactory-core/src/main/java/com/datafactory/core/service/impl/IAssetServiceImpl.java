package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.asset.AssetCreateRequest;
import com.datafactory.common.model.dto.asset.AssetDirectoryCreateRequest;
import com.datafactory.common.model.dto.asset.AssetDirectoryUpdateRequest;
import com.datafactory.common.model.dto.asset.AssetFieldDTO;
import com.datafactory.common.model.dto.asset.AssetUpdateRequest;
import com.datafactory.common.model.vo.asset.AssetDetailVo;
import com.datafactory.common.model.vo.asset.AssetDetailVo.AssetDirectorySimpleVo;
import com.datafactory.common.model.vo.asset.AssetDirectoryVo;
import com.datafactory.common.model.vo.asset.AssetFieldVo;
import com.datafactory.common.model.vo.asset.AssetListVo;
import com.datafactory.core.domain.entity.Asset;
import com.datafactory.core.domain.entity.AssetDirectory;
import com.datafactory.core.domain.entity.AssetDirectoryRel;
import com.datafactory.core.domain.entity.AssetField;
import com.datafactory.core.domain.mapper.AssetDirectoryMapper;
import com.datafactory.core.domain.mapper.AssetDirectoryRelMapper;
import com.datafactory.core.domain.mapper.AssetFieldMapper;
import com.datafactory.core.domain.mapper.AssetMapper;
import com.datafactory.core.service.IAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据资产服务实现类
 *
 * 处理资产目录树的查询与 CRUD，以及资产的增删改查、状态流转和批量操作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IAssetServiceImpl extends ServiceImpl<AssetMapper, Asset> implements IAssetService {

    private final AssetDirectoryMapper assetDirectoryMapper;
    private final AssetDirectoryRelMapper assetDirectoryRelMapper;
    private final AssetFieldMapper assetFieldMapper;

    // ==================== 资产目录 ====================

    @Override
    public List<AssetDirectoryVo> getDirectoryTree() {
        // 查询所有目录，按排序号升序
        List<AssetDirectory> allDirectories = assetDirectoryMapper.selectList(
                new LambdaQueryWrapper<AssetDirectory>().orderByAsc(AssetDirectory::getSortOrder));
        // 按 parentId 分组
        Map<Long, List<AssetDirectoryVo>> childrenMap = allDirectories.stream()
                .map(this::toDirectoryVo)
                .collect(Collectors.groupingBy(AssetDirectoryVo::getParentId));
        // 从顶级节点开始构建树
        List<AssetDirectoryVo> tree = new ArrayList<>();
        for (AssetDirectoryVo vo : childrenMap.getOrDefault(0L, Collections.emptyList())) {
            buildTree(vo, childrenMap);
            tree.add(vo);
        }
        return tree;
    }

    /**
     * 递归构建子树
     */
    private void buildTree(AssetDirectoryVo parent, Map<Long, List<AssetDirectoryVo>> childrenMap) {
        List<AssetDirectoryVo> children = childrenMap.getOrDefault(parent.getId(), Collections.emptyList());
        parent.setChildren(children);
        for (AssetDirectoryVo child : children) {
            buildTree(child, childrenMap);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetDirectoryVo createDirectory(AssetDirectoryCreateRequest request) {
        AssetDirectory directory = new AssetDirectory();
        directory.setName(request.getName());
        directory.setParentId(request.getParentId());
        directory.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        // 计算层级
        if (request.getParentId() != null && request.getParentId() != 0) {
            // 1. 查询父级目录
            AssetDirectory parent = assetDirectoryMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException(StatusCode.NOT_FOUND, "父级目录不存在");
            }
            // 2. 检查父级目录下是否已有关联资产（有资产的目录下不可新建子目录）
            Long assetCount = assetDirectoryRelMapper.selectCount(
                    new LambdaQueryWrapper<AssetDirectoryRel>()
                            .eq(AssetDirectoryRel::getDirectoryId, request.getParentId()));
            if (assetCount > 0) {
                throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "已有资产的目录下不可新建子目录");
            }
            directory.setLevel(parent.getLevel() + 1);
        } else {
            directory.setLevel(1);
            directory.setParentId(0L);
        }

        assetDirectoryMapper.insert(directory);
        log.info("新增资产目录成功：name={}, id={}", directory.getName(), directory.getId());
        return toDirectoryVo(directory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDirectory(Long id, AssetDirectoryUpdateRequest request) {
        AssetDirectory directory = assetDirectoryMapper.selectById(id);
        if (directory == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "目录不存在");
        }
        directory.setName(request.getName());
        if (request.getSortOrder() != null) {
            directory.setSortOrder(request.getSortOrder());
        }
        assetDirectoryMapper.updateById(directory);
        log.info("编辑资产目录成功：id={}, name={}", id, request.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDirectory(Long id) {
        AssetDirectory directory = assetDirectoryMapper.selectById(id);
        if (directory == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "目录不存在");
        }
        // 检查是否有子目录
        Long childCount = assetDirectoryMapper.selectCount(
                new LambdaQueryWrapper<AssetDirectory>().eq(AssetDirectory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "目录下存在子目录，无法删除");
        }
        // 检查是否有关联资产
        Long relCount = assetDirectoryRelMapper.selectCount(
                new LambdaQueryWrapper<AssetDirectoryRel>().eq(AssetDirectoryRel::getDirectoryId, id));
        if (relCount > 0) {
            throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "目录下已关联资产，无法删除");
        }
        assetDirectoryMapper.deleteById(id);
        log.info("删除资产目录成功：id={}", id);
    }

    /**
     * 将实体转为目录 VO
     */
    private AssetDirectoryVo toDirectoryVo(AssetDirectory entity) {
        AssetDirectoryVo vo = new AssetDirectoryVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setParentId(entity.getParentId());
        vo.setLevel(entity.getLevel());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreateTime(entity.getCreateTime());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    // ==================== 资产列表与详情 ====================

    @Override
    public Page<AssetListVo> getAssetList(Integer pageNum, Integer pageSize, String keyword,
                                          String status, Long directoryId) {
        Page<Asset> page = new Page<>(pageNum, pageSize);
        // 排序：状态优先（0→1→2），再按更新时间倒序
        page.addOrder(OrderItem.asc("status"));
        page.addOrder(OrderItem.desc("update_time"));

        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索：模糊匹配中文名称、英文名称
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Asset::getAssetName, keyword)
                    .or().like(Asset::getEnglishName, keyword));
        }

        // 状态筛选（多值逗号分隔）
        if (status != null && !status.isBlank()) {
            String[] statusArr = status.split(",");
            wrapper.and(w -> {
                for (String s : statusArr) {
                    w.or().eq(Asset::getStatus, Integer.parseInt(s.trim()));
                }
            });
        }

        // 目录筛选：查询该目录及其所有后代目录下的资产
        if (directoryId != null) {
            Set<Long> dirIds = getDirectoryAndDescendantIds(directoryId);
            if (!dirIds.isEmpty()) {
                List<Long> assetIds = assetDirectoryRelMapper.selectList(
                                new LambdaQueryWrapper<AssetDirectoryRel>()
                                        .in(AssetDirectoryRel::getDirectoryId, dirIds))
                        .stream()
                        .map(AssetDirectoryRel::getAssetId)
                        .distinct()
                        .collect(Collectors.toList());
                if (assetIds.isEmpty()) {
                    Page<AssetListVo> emptyPage = new Page<>(pageNum, pageSize, 0);
                    emptyPage.setRecords(Collections.emptyList());
                    return emptyPage;
                }
                wrapper.in(Asset::getId, assetIds);
            }
        }

        page = baseMapper.selectPage(page, wrapper);

        // 转换为 VO，填充 directoryIds
        Page<AssetListVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<AssetListVo> voList = page.getRecords().stream()
                .map(this::toAssetListVo)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AssetDetailVo getAssetDetail(Long id) {
        Asset asset = baseMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "资产不存在");
        }
        return toAssetDetailVo(asset);
    }

    /**
     * 将 Asset 转为列表 VO，并填充 directoryIds
     */
    private AssetListVo toAssetListVo(Asset asset) {
        AssetListVo vo = new AssetListVo();
        vo.setId(asset.getId());
        vo.setAssetName(asset.getAssetName());
        vo.setEnglishName(asset.getEnglishName());
        vo.setDescription(asset.getDescription());
        vo.setStatus(asset.getStatus());
        vo.setCreateTime(asset.getCreateTime());
        vo.setUpdateTime(asset.getUpdateTime());

        // 查询关联的目录ID列表
        List<Long> dirIds = assetDirectoryRelMapper.selectList(
                        new LambdaQueryWrapper<AssetDirectoryRel>()
                                .eq(AssetDirectoryRel::getAssetId, asset.getId()))
                .stream()
                .map(AssetDirectoryRel::getDirectoryId)
                .collect(Collectors.toList());
        vo.setDirectoryIds(dirIds);
        return vo;
    }

    /**
     * 将 Asset 转为详情 VO
     */
    private AssetDetailVo toAssetDetailVo(Asset asset) {
        AssetDetailVo vo = new AssetDetailVo();
        vo.setId(asset.getId());
        vo.setAssetName(asset.getAssetName());
        vo.setEnglishName(asset.getEnglishName());
        vo.setDescription(asset.getDescription());
        vo.setStatus(asset.getStatus());
        vo.setCreateTime(asset.getCreateTime());
        vo.setUpdateTime(asset.getUpdateTime());

        // 查询关联目录
        List<AssetDirectoryRel> rels = assetDirectoryRelMapper.selectList(
                new LambdaQueryWrapper<AssetDirectoryRel>()
                        .eq(AssetDirectoryRel::getAssetId, asset.getId()));
        List<Long> dirIds = rels.stream().map(AssetDirectoryRel::getDirectoryId).collect(Collectors.toList());
        vo.setDirectoryIds(dirIds);

        // 填充目录简单信息
        if (!dirIds.isEmpty()) {
            List<AssetDirectory> dirs = assetDirectoryMapper.selectBatchIds(dirIds);
            List<AssetDirectorySimpleVo> dirVos = dirs.stream().map(d -> {
                AssetDirectorySimpleVo dv = new AssetDirectorySimpleVo();
                dv.setId(d.getId());
                dv.setName(d.getName());
                return dv;
            }).collect(Collectors.toList());
            vo.setDirectories(dirVos);
        } else {
            vo.setDirectories(Collections.emptyList());
        }

        // 查询字段定义，按排序号升序
        List<AssetField> fields = assetFieldMapper.selectList(
                new LambdaQueryWrapper<AssetField>()
                        .eq(AssetField::getAssetId, asset.getId())
                        .orderByAsc(AssetField::getSortOrder));
        List<AssetFieldVo> fieldVos = fields.stream().map(f -> {
            AssetFieldVo fv = new AssetFieldVo();
            fv.setId(f.getId());
            fv.setEnglishFieldName(f.getEnglishFieldName());
            fv.setChineseFieldName(f.getChineseFieldName());
            fv.setDescription(f.getDescription());
            fv.setStandardId(f.getStandardId());
            fv.setSortOrder(f.getSortOrder());
            return fv;
        }).collect(Collectors.toList());
        vo.setFields(fieldVos);

        return vo;
    }

    // ==================== 资产 CRUD ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetDetailVo createAsset(AssetCreateRequest request) {
        // 1. 校验 assetName 唯一性
        Long nameCount = lambdaQuery().eq(Asset::getAssetName, request.getAssetName()).count();
        if (nameCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "中文名称已存在");
        }
        // 2. 校验 englishName 唯一性
        Long enCount = lambdaQuery().eq(Asset::getEnglishName, request.getEnglishName()).count();
        if (enCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "英文名称已存在");
        }
        // 3. 校验目录ID均为叶子节点（无子目录）
        validateDirectoriesAreLeaves(request.getDirectoryIds());

        // 4. 创建资产
        Asset asset = new Asset();
        asset.setAssetName(request.getAssetName());
        asset.setEnglishName(request.getEnglishName());
        asset.setDescription(request.getDescription());
        asset.setStatus(0); // DRAFT
        baseMapper.insert(asset);

        // 5. 保存目录关联
        saveAssetDirectoryRels(asset.getId(), request.getDirectoryIds());

        // 6. 保存字段定义
        saveAssetFields(asset.getId(), request.getFields());

        log.info("新增数据资产成功：name={}, id={}", asset.getAssetName(), asset.getId());
        return toAssetDetailVo(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAsset(Long id, AssetUpdateRequest request) {
        Asset asset = baseMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "资产不存在");
        }
        // 仅未发布和已停用状态可编辑
        if (asset.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布状态的资产不可编辑");
        }

        // 校验 assetName 唯一性（排除自身）
        Long nameCount = lambdaQuery()
                .eq(Asset::getAssetName, request.getAssetName())
                .ne(Asset::getId, id)
                .count();
        if (nameCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "中文名称已存在");
        }
        // 校验 englishName 唯一性（排除自身）
        Long enCount = lambdaQuery()
                .eq(Asset::getEnglishName, request.getEnglishName())
                .ne(Asset::getId, id)
                .count();
        if (enCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "英文名称已存在");
        }
        // 校验目录ID均为叶子节点
        validateDirectoriesAreLeaves(request.getDirectoryIds());

        // 更新资产基本信息
        asset.setAssetName(request.getAssetName());
        asset.setEnglishName(request.getEnglishName());
        asset.setDescription(request.getDescription());
        baseMapper.updateById(asset);

        // 更新目录关联：先删后增
        assetDirectoryRelMapper.delete(
                new LambdaQueryWrapper<AssetDirectoryRel>().eq(AssetDirectoryRel::getAssetId, id));
        saveAssetDirectoryRels(id, request.getDirectoryIds());

        // 更新字段定义：先删后增
        assetFieldMapper.delete(
                new LambdaQueryWrapper<AssetField>().eq(AssetField::getAssetId, id));
        saveAssetFields(id, request.getFields());

        log.info("编辑数据资产成功：id={}, name={}", id, request.getAssetName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAsset(Long id) {
        Asset asset = baseMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "资产不存在");
        }
        if (asset.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅未发布状态的资产可删除");
        }
        // 删除关联关系
        assetDirectoryRelMapper.delete(
                new LambdaQueryWrapper<AssetDirectoryRel>().eq(AssetDirectoryRel::getAssetId, id));
        // 删除字段定义
        assetFieldMapper.delete(
                new LambdaQueryWrapper<AssetField>().eq(AssetField::getAssetId, id));
        // 删除资产
        baseMapper.deleteById(id);
        log.info("删除数据资产成功：id={}", id);
    }

    // ==================== 状态流转 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAsset(Long id) {
        Asset asset = baseMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "资产不存在");
        }
        // 仅未发布(0)和已停用(2)可发布
        if (asset.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "该资产已是已发布状态");
        }
        lambdaUpdate().eq(Asset::getId, id).set(Asset::getStatus, 1).update();
        log.info("发布数据资产成功：id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableAsset(Long id) {
        Asset asset = baseMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "资产不存在");
        }
        // 仅已发布(1)可停用
        if (asset.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅已发布状态的资产可停用");
        }
        lambdaUpdate().eq(Asset::getId, id).set(Asset::getStatus, 2).update();
        log.info("停用数据资产成功：id={}", id);
    }

    // ==================== 批量操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选资产
        List<Asset> assetList = lambdaQuery().in(Asset::getId, ids).list();

        // 2. 校验数据完整性
        if (assetList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分资产不存在");
        }

        // 3. 校验是否包含已发布资产（未发布和已停用均可发布）
        boolean hasInvalid = assetList.stream().anyMatch(a -> a.getStatus() == 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED,
                    "所选资产中包含已发布状态的数据，操作不合法");
        }

        // 4. 批量更新为已发布
        lambdaUpdate().in(Asset::getId, ids).set(Asset::getStatus, 1).update();
        log.info("批量发布数据资产成功：ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisable(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选资产
        List<Asset> assetList = lambdaQuery().in(Asset::getId, ids).list();

        // 2. 校验数据完整性
        if (assetList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分资产不存在");
        }

        // 3. 校验是否均为已发布状态
        boolean hasInvalid = assetList.stream().anyMatch(a -> a.getStatus() != 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED,
                    "所选资产中包含未发布或已停用状态的数据，操作不合法");
        }

        // 4. 批量更新为已停用
        lambdaUpdate().in(Asset::getId, ids).set(Asset::getStatus, 2).update();
        log.info("批量停用数据资产成功：ids={}", ids);
    }

    // ==================== 私有工具方法 ====================

    /**
     * 获取指定目录及其所有后代目录的ID集合
     *
     * @param directoryId 起始目录ID
     * @return 包含自身及所有后代的ID集合
     */
    private Set<Long> getDirectoryAndDescendantIds(Long directoryId) {
        Set<Long> result = new HashSet<>();
        result.add(directoryId);
        // 查询所有目录
        List<AssetDirectory> allDirs = assetDirectoryMapper.selectList(null);
        Map<Long, List<AssetDirectory>> parentMap = allDirs.stream()
                .collect(Collectors.groupingBy(AssetDirectory::getParentId));
        // BFS 收集后代
        Queue<Long> queue = new LinkedList<>();
        queue.add(directoryId);
        while (!queue.isEmpty()) {
            Long parentId = queue.poll();
            List<AssetDirectory> children = parentMap.getOrDefault(parentId, Collections.emptyList());
            for (AssetDirectory child : children) {
                Long childId = child.getId();
                if (result.add(childId)) {
                    queue.add(childId);
                }
            }
        }
        return result;
    }

    /**
     * 校验目录ID对应的目录均为叶子节点（无子目录）
     *
     * @param directoryIds 待校验的目录ID列表
     */
    private void validateDirectoriesAreLeaves(List<Long> directoryIds) {
        if (directoryIds == null || directoryIds.isEmpty()) {
            return;
        }
        for (Long dirId : directoryIds) {
            Long childCount = assetDirectoryMapper.selectCount(
                    new LambdaQueryWrapper<AssetDirectory>().eq(AssetDirectory::getParentId, dirId));
            if (childCount > 0) {
                // 查找该目录的名称用于友好错误提示
                AssetDirectory dir = assetDirectoryMapper.selectById(dirId);
                String dirName = dir != null ? dir.getName() : String.valueOf(dirId);
                throw new BusinessException(StatusCode.BAD_REQUEST,
                        "目录「" + dirName + "」不是叶子节点，请选择叶子节点目录");
            }
        }
    }

    /**
     * 保存目录关联关系
     *
     * @param assetId      资产ID
     * @param directoryIds 目录ID列表
     */
    private void saveAssetDirectoryRels(Long assetId, List<Long> directoryIds) {
        if (directoryIds == null || directoryIds.isEmpty()) {
            return;
        }
        for (Long dirId : directoryIds) {
            AssetDirectoryRel rel = new AssetDirectoryRel();
            rel.setAssetId(assetId);
            rel.setDirectoryId(dirId);
            assetDirectoryRelMapper.insert(rel);
        }
    }

    /**
     * 保存字段定义
     *
     * @param assetId   资产ID
     * @param fieldDTOs 字段定义列表
     */
    private void saveAssetFields(Long assetId, List<AssetFieldDTO> fieldDTOs) {
        if (fieldDTOs == null || fieldDTOs.isEmpty()) {
            return;
        }
        for (int i = 0; i < fieldDTOs.size(); i++) {
            AssetFieldDTO dto = fieldDTOs.get(i);
            AssetField field = new AssetField();
            field.setAssetId(assetId);
            field.setEnglishFieldName(dto.getEnglishFieldName());
            field.setChineseFieldName(dto.getChineseFieldName());
            field.setDescription(dto.getDescription());
            field.setStandardId(dto.getStandardId());
            field.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : i + 1);
            assetFieldMapper.insert(field);
        }
    }
}
