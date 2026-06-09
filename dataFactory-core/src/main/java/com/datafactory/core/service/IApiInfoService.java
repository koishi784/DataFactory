package com.datafactory.core.service;

import com.datafactory.common.model.dto.api.ApiCreateRequest;
import com.datafactory.common.model.dto.api.ApiTestRequest;
import com.datafactory.common.model.dto.api.ApiUpdateRequest;
import com.datafactory.common.model.dto.api.BatchCategoryRequest;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.vo.api.ApiDetailVo;
import com.datafactory.common.model.vo.api.ApiInfoVo;
import com.datafactory.common.model.vo.api.ApiTestResultVo;
import com.datafactory.core.domain.entity.ApiInfo;

import java.util.List;

/**
 * 接口注册管理服务接口
 *
 * 提供注册接口的 CRUD、状态管理（发布/停用/删除）、批量操作、测试调用等业务方法
 */
public interface IApiInfoService {

    /**
     * 分页查询接口列表
     *
     * @param pageNum   页码，从 1 开始
     * @param pageSize  每页条数，默认 20，最大 100
     * @param keyword   关键词，模糊匹配接口名称、接口说明
     * @param status    状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @param categoryId 分类ID筛选
     * @param source    接口来源筛选
     * @param sortOrder 更新时间排序：asc / desc，默认 desc
     * @return 分页结果，包含 records、total、pageNum、pageSize、totalPages
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<ApiInfoVo> getApiList(
            Integer pageNum, Integer pageSize, String keyword, String status,
            Long categoryId, String source, String sortOrder);

    /**
     * 查询接口详情
     *
     * @param id 接口ID
     * @return 接口完整信息（含 headers、requestParams、responseExample、remark）
     */
    ApiDetailVo getApiDetail(Long id);

    /**
     * 新增注册接口
     *
     * 创建接口基本信息，同时保存请求头和请求参数配置，初始状态为 DRAFT(0)。
     * 接口名称（apiName）和接口 URL（url）均为全局唯一。
     *
     * @param request 新增接口请求参数
     * @return 创建的接口实体（状态为 DRAFT）
     */
    ApiInfo createApi(ApiCreateRequest request);

    /**
     * 编辑接口
     *
     * 仅未发布(0)和已停用(2)状态可编辑。已停用状态下不可编辑 url（Path）字段。
     * 已发布(1)状态不可编辑。接口名称（apiName）和接口 URL（url）修改后需保持全局唯一。
     *
     * @param id      接口ID
     * @param request 编辑接口请求参数
     */
    void updateApi(Long id, ApiUpdateRequest request);

    /**
     * 发布接口
     *
     * 将未发布(0)或已停用(2)状态的接口发布为已发布(1)。
     *
     * @param id 接口ID
     */
    void publishApi(Long id);

    /**
     * 停用接口
     *
     * 将 PUBLISHED 状态的接口变更为 DISABLED
     *
     * @param id 接口ID
     */
    void disableApi(Long id);

    /**
     * 删除接口
     *
     * 仅可删除 DRAFT 状态的接口
     *
     * @param id 接口ID
     */
    void deleteApi(Long id);

    /**
     * 批量发布接口
     *
     * @param request 批量操作请求（ID 列表）
     */
    void batchPublish(BatchIdsRequest request);

    /**
     * 批量停用接口
     *
     * @param request 批量操作请求（ID 列表）
     */
    void batchDisable(BatchIdsRequest request);

    /**
     * 批量修改接口分类
     *
     * @param request 批量分类请求（ID 列表 + 目标分类ID）
     */
    void batchCategory(BatchCategoryRequest request);

    /**
     * 接口测试调用
     *
     * @param id      接口ID
     * @param request 测试调用请求（临时参数值）
     * @return 测试调用结果
     */
    ApiTestResultVo testApi(Long id, ApiTestRequest request);
}
