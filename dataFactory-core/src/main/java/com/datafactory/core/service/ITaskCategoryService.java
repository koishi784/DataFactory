package com.datafactory.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.datafactory.common.model.dto.task.TaskCategoryRequest;
import com.datafactory.common.model.vo.task.TaskCategoryVo;
import com.datafactory.core.domain.entity.TaskCategory;

import java.util.List;

/**
 * 任务分类服务接口
 *
 * 提供任务分类的树形查询、新增、编辑、删除等业务操作
 */
public interface ITaskCategoryService extends IService<TaskCategory> {

    /**
     * 查询任务分类树
     *
     * 获取全量任务分类数据，在内存中递归构建为树形结构返回
     *
     * @return 分类树根节点列表
     */
    List<TaskCategoryVo> getCategoryTree();

    /**
     * 新增任务分类
     *
     * @param request 新增分类请求参数
     * @return 创建的完整分类对象
     */
    TaskCategoryVo createCategory(TaskCategoryRequest request);

    /**
     * 编辑任务分类
     *
     * @param id      分类ID
     * @param request 编辑分类请求参数
     * @return 更新后的分类对象
     */
    TaskCategoryVo updateCategory(Long id, TaskCategoryRequest request);

    /**
     * 删除任务分类
     *
     * 仅能删除无子分类且无关联任务的分类
     *
     * @param id 分类ID
     * @throws com.datafactory.common.exception.BusinessException 分类下存在子分类或关联任务时抛出
     */
    void deleteCategory(Long id);
}
