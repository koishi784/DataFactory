package com.datafactory.core.service;

import com.datafactory.common.model.dto.api.ApiCategoryCreateRequest;
import com.datafactory.common.model.dto.api.ApiCategoryUpdateRequest;
import com.datafactory.common.model.vo.api.ApiCategoryTreeVo;
import com.datafactory.core.domain.entity.ApiCategory;

import java.util.List;

/**
 * 接口分类服务接口
 *
 * 提供接口分类的树形查询、新增、编辑、删除等业务操作
 */
public interface IApiCategoryService {

    /**
     * 查询接口分类树
     *
     * 获取全量接口分类数据，在内存中递归构建为树形结构返回
     *
     * @return 分类树根节点列表（顶级节点列表）
     */
    List<ApiCategoryTreeVo> getCategoryTree();

    /**
     * 新增分类
     *
     * @param request 新增分类请求参数（名称、父级ID、排序号）
     * @return 创建的完整分类实体
     */
    ApiCategory createCategory(ApiCategoryCreateRequest request);

    /**
     * 编辑分类
     *
     * @param id      分类ID
     * @param request 编辑分类请求参数（名称、排序号）
     */
    void updateCategory(Long id, ApiCategoryUpdateRequest request);

    /**
     * 删除分类
     *
     * 仅能删除无子分类且无关联接口的分类
     *
     * @param id 分类ID
     * @throws com.datafactory.common.exception.BusinessException 分类下存在子分类或关联接口时抛出
     */
    void deleteCategory(Long id);
}
