package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.task.TaskCategoryRequest;
import com.datafactory.common.model.vo.task.TaskCategoryVo;
import com.datafactory.core.domain.entity.Task;
import com.datafactory.core.domain.entity.TaskCategory;
import com.datafactory.core.domain.mapper.TaskCategoryMapper;
import com.datafactory.core.domain.mapper.TaskMapper;
import com.datafactory.core.service.ITaskCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务分类服务实现类
 *
 * 实现任务分类的树形结构查询、CRUD 等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ITaskCategoryServiceImpl extends ServiceImpl<TaskCategoryMapper, TaskCategory> implements ITaskCategoryService {

    private final TaskMapper taskMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询任务分类树
     *
     * 全量加载分类数据，按 parentId 分组后在内存中递归构建树形结构，
     * 同级节点按 sortOrder 升序排列。
     *
     * @return 分类树根节点列表
     */
    @Override
    public List<TaskCategoryVo> getCategoryTree() {
        // 1. 查询所有未删除的分类
        List<TaskCategory> allCategories = lambdaQuery()
                .orderByAsc(TaskCategory::getSortOrder)
                .list();

        // 2. 按 parentId 分组
        Map<Long, List<TaskCategory>> parentIdMap = allCategories.stream()
                .collect(Collectors.groupingBy(TaskCategory::getParentId));

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
    private List<TaskCategoryVo> buildTree(Map<Long, List<TaskCategory>> parentIdMap, Long parentId) {
        List<TaskCategory> children = parentIdMap.getOrDefault(parentId, new ArrayList<>());

        // 同级按 sortOrder 升序排序
        children.sort(Comparator.comparingInt(TaskCategory::getSortOrder));

        List<TaskCategoryVo> treeVos = new ArrayList<>();
        for (TaskCategory category : children) {
            TaskCategoryVo vo = new TaskCategoryVo();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setParentId(category.getParentId());
            vo.setLevel(category.getLevel());
            vo.setSortOrder(category.getSortOrder());
            vo.setCreateTime(category.getCreateTime() != null ? category.getCreateTime().format(DTF) : null);
            // 递归构建子节点
            vo.setChildren(buildTree(parentIdMap, category.getId()));
            treeVos.add(vo);
        }
        return treeVos;
    }

    /**
     * 新增任务分类
     *
     * 根据 parentId 自动计算层级：顶级（parentId=0）层级为 1，子分类层级为父级层级 + 1。
     * 同一父级下子分类名称不可重复。
     *
     * @param request 新增分类请求参数
     * @return 创建的完整分类对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskCategoryVo createCategory(TaskCategoryRequest request) {
        // 1. 校验同一父级下名称唯一性
        Long existsCount = lambdaQuery()
                .eq(TaskCategory::getParentId, request.getParentId())
                .eq(TaskCategory::getName, request.getName())
                .count();
        if (existsCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "同一父级下已存在同名分类");
        }

        // 2. 计算层级
        int level;
        if (request.getParentId() == 0L) {
            level = 1;
        } else {
            TaskCategory parent = lambdaQuery()
                    .eq(TaskCategory::getId, request.getParentId())
                    .one();
            if (parent == null) {
                throw new BusinessException(StatusCode.NOT_FOUND, "父级分类不存在");
            }
            level = parent.getLevel() + 1;
        }

        // 3. 构建实体
        TaskCategory category = new TaskCategory();
        category.setName(request.getName());
        category.setParentId(request.getParentId());
        category.setLevel(level);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        // 4. 保存
        save(category);

        log.info("新增任务分类成功：name={}, parentId={}", request.getName(), request.getParentId());
        return convertToVo(category);
    }

    /**
     * 编辑任务分类
     *
     * 修改后的名称不可与同一父级下其他分类重名。
     *
     * @param id      分类ID
     * @param request 编辑分类请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskCategoryVo updateCategory(Long id, TaskCategoryRequest request) {
        // 1. 校验分类是否存在
        TaskCategory category = lambdaQuery()
                .eq(TaskCategory::getId, id)
                .one();
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "分类不存在");
        }

        // 2. 校验同一父级下名称唯一性（排除自身）
        Long existsCount = lambdaQuery()
                .eq(TaskCategory::getParentId, category.getParentId())
                .eq(TaskCategory::getName, request.getName())
                .ne(TaskCategory::getId, id)
                .count();
        if (existsCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "同一父级下已存在同名分类");
        }

        // 3. 更新字段
        category.setName(request.getName());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        // 4. 保存
        lambdaUpdate()
                .eq(TaskCategory::getId, id)
                .update(category);

        log.info("编辑任务分类成功：id={}, name={}", id, request.getName());
        return convertToVo(category);
    }

    /**
     * 删除任务分类
     *
     * 仅能删除无子分类且无关联任务的分类。
     *
     * @param id 分类ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 1. 校验分类是否存在
        TaskCategory category = lambdaQuery()
                .eq(TaskCategory::getId, id)
                .one();
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "分类不存在");
        }

        // 2. 检查是否存在子分类
        Long childCount = lambdaQuery()
                .eq(TaskCategory::getParentId, id)
                .count();
        if (childCount > 0) {
            throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "分类下存在子分类，无法删除");
        }

        // 3. 检查是否存在关联任务
        Long taskCount = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getCategoryId, id)
        );
        if (taskCount > 0) {
            throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "分类下存在关联任务，无法删除");
        }

        // 4. 执行删除（逻辑删除由 BaseEntity 的 @TableLogic 自动处理）
        removeById(id);

        log.info("删除任务分类成功：id={}, name={}", id, category.getName());
    }

    /**
     * 将 TaskCategory 实体转换为 TaskCategoryVo
     */
    private TaskCategoryVo convertToVo(TaskCategory category) {
        TaskCategoryVo vo = new TaskCategoryVo();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setLevel(category.getLevel());
        vo.setSortOrder(category.getSortOrder());
        vo.setChildren(new ArrayList<>());
        vo.setCreateTime(category.getCreateTime() != null ? category.getCreateTime().format(DTF) : null);
        return vo;
    }
}
