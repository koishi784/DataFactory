package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.api.ApiCategoryCreateRequest;
import com.datafactory.common.model.dto.api.ApiCategoryUpdateRequest;
import com.datafactory.common.model.vo.api.ApiCategoryTreeVo;
import com.datafactory.core.domain.entity.ApiCategory;
import com.datafactory.core.domain.entity.ApiInfo;
import com.datafactory.core.domain.mapper.ApiCategoryMapper;
import com.datafactory.core.domain.mapper.ApiInfoMapper;
import com.datafactory.core.service.IApiCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口分类服务实现类
 *
 * 实现接口分类的树形结构查询、CRUD 等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IApiCategoryServiceImpl extends ServiceImpl<ApiCategoryMapper, ApiCategory> implements IApiCategoryService {

    private final ApiInfoMapper apiInfoMapper;

    /**
     * 查询接口分类树
     *
     * 全量加载分类数据，按 parentId 分组后在内存中递归构建树形结构，
     * 同级节点按 sortOrder 升序排列。
     *
     * @return 分类树根节点列表
     */
    @Override
    public List<ApiCategoryTreeVo> getCategoryTree() {
        // 1. 查询所有未删除的分类
        List<ApiCategory> allCategories = lambdaQuery()
                .orderByAsc(ApiCategory::getSortOrder)
                .list();

        // 2. 按 parentId 分组
        Map<Long, List<ApiCategory>> parentIdMap = allCategories.stream()
                .collect(Collectors.groupingBy(ApiCategory::getParentId));

        // 3. 从根节点（parentId=0）开始递归构建树
        return buildTree(parentIdMap, 0L);
    }

    /**
     * 递归构建分类树
     *
     * @param parentIdMap 按 parentId 分组后的分类映射
     * @param parentId    当前父节点ID
     * @return 当前层级的分类树节点列表
     */
    private List<ApiCategoryTreeVo> buildTree(Map<Long, List<ApiCategory>> parentIdMap, Long parentId) {
        List<ApiCategory> children = parentIdMap.getOrDefault(parentId, new ArrayList<>());

        // 同级按 sortOrder 升序排序
        children.sort(Comparator.comparingInt(ApiCategory::getSortOrder));

        List<ApiCategoryTreeVo> treeVos = new ArrayList<>();
        for (ApiCategory category : children) {
            ApiCategoryTreeVo vo = new ApiCategoryTreeVo();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setParentId(category.getParentId());
            vo.setLevel(category.getLevel());
            vo.setSortOrder(category.getSortOrder());
            vo.setCreateTime(category.getCreateTime());
            // 递归构建子节点
            vo.setChildren(buildTree(parentIdMap, category.getId()));
            treeVos.add(vo);
        }
        return treeVos;
    }

    /**
     * 新增分类
     *
     * 根据 parentId 自动计算层级：顶级（parentId=0）层级为 1，子分类层级为父级层级 + 1。
     * 同一父级下子分类名称不可重复。
     *
     * @param request 新增分类请求参数
     * @return 创建的完整分类实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiCategory createCategory(ApiCategoryCreateRequest request) {
        // 0. 校验同一父级下名称唯一性
        Long existsCount = lambdaQuery()
                .eq(ApiCategory::getParentId, request.getParentId())
                .eq(ApiCategory::getName, request.getName())
                .count();
        if (existsCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "同一父级下已存在同名分类");
        }

        // 1. 计算层级
        int level;
        if (request.getParentId() == 0L) {
            level = 1;
        } else {
            // 查询父级分类
            ApiCategory parent = lambdaQuery()
                    .eq(ApiCategory::getId, request.getParentId())
                    .one();
            if (parent == null) {
                throw new BusinessException(StatusCode.NOT_FOUND, "父级分类不存在");
            }
            level = parent.getLevel() + 1;
        }

        // 2. 构建实体
        ApiCategory category = new ApiCategory();
        category.setName(request.getName());
        category.setParentId(request.getParentId());
        category.setLevel(level);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        // 3. 保存
        save(category);

        log.info("新增接口分类成功：name={}, parentId={}", request.getName(), request.getParentId());
        return category;
    }

    /**
     * 编辑分类
     *
     * 修改后的名称不可与同一父级下其他分类重名。
     *
     * @param id      分类ID
     * @param request 编辑分类请求参数
     * @throws BusinessException 分类不存在或名称冲突时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, ApiCategoryUpdateRequest request) {
        // 1. 校验分类是否存在
        ApiCategory category = lambdaQuery()
                .eq(ApiCategory::getId, id)
                .one();
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "分类不存在");
        }

        // 2. 校验同一父级下名称唯一性（排除自身）
        Long existsCount = lambdaQuery()
                .eq(ApiCategory::getParentId, category.getParentId())
                .eq(ApiCategory::getName, request.getName())
                .ne(ApiCategory::getId, id)
                .count();
        if (existsCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "同一父级下已存在同名分类");
        }

        // 3. 更新字段
        category.setName(request.getName());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        // 3. 保存
        lambdaUpdate()
                .eq(ApiCategory::getId, id)
                .update(category);

        log.info("编辑接口分类成功：id={}, name={}", id, request.getName());
    }

    /**
     * 删除分类
     *
     * 仅能删除无子分类且无关联接口的分类。
     *
     * @param id 分类ID
     * @throws BusinessException 分类不存在、存在子分类或有关联接口时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 1. 校验分类是否存在
        ApiCategory category = lambdaQuery()
                .eq(ApiCategory::getId, id)
                .one();
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "分类不存在");
        }

        // 2. 检查是否存在子分类
        Long childCount = lambdaQuery()
                .eq(ApiCategory::getParentId, id)
                .count();
        if (childCount > 0) {
            throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "分类下存在子分类，无法删除");
        }

        // 3. 检查是否存在关联接口
        Long apiCount = apiInfoMapper.selectCount(
                new LambdaQueryWrapper<ApiInfo>()
                        .eq(ApiInfo::getCategoryId, id)
        );
        if (apiCount > 0) {
            throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "分类下存在关联接口，无法删除");
        }

        // 4. 执行删除（逻辑删除由 BaseEntity 的 @TableLogic 自动处理）
        removeById(id);

        log.info("删除接口分类成功：id={}, name={}", id, category.getName());
    }
}
