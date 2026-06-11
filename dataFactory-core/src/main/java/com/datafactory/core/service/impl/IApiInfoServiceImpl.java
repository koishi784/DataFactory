package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.utils.StatusUtils;
import com.datafactory.common.model.dto.api.ApiCreateRequest;
import com.datafactory.common.model.dto.api.ApiTestRequest;
import com.datafactory.common.model.dto.api.ApiUpdateRequest;
import com.datafactory.common.model.dto.api.BatchCategoryRequest;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.vo.api.ApiDetailVo;
import com.datafactory.common.model.vo.api.ApiInfoVo;
import com.datafactory.common.model.vo.api.ApiTestResultVo;
import com.datafactory.common.model.vo.api.HeaderConfigVo;
import com.datafactory.common.model.vo.api.ParamConfigVo;
import com.datafactory.core.domain.entity.ApiCategory;
import com.datafactory.core.domain.entity.ApiHeader;
import com.datafactory.core.domain.entity.ApiInfo;
import com.datafactory.core.domain.entity.ApiParam;
import com.datafactory.core.domain.mapper.ApiCategoryMapper;
import com.datafactory.core.domain.mapper.ApiHeaderMapper;
import com.datafactory.core.domain.mapper.ApiInfoMapper;
import com.datafactory.core.domain.mapper.ApiParamMapper;
import com.datafactory.core.service.IApiInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口注册管理服务实现类
 *
 * 实现注册接口的 CRUD、状态管理、批量操作、测试调用等完整业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IApiInfoServiceImpl extends ServiceImpl<ApiInfoMapper, ApiInfo> implements IApiInfoService {

    private final ApiCategoryMapper apiCategoryMapper;
    private final ApiHeaderMapper apiHeaderMapper;
    private final ApiParamMapper apiParamMapper;
    private final ApiInfoMapper apiInfoMapper;

    /**
     * 分页查询接口列表
     *
     * 支持多条件筛选（关键词、状态、分类、来源），排序规则：
     * 优先级一：按状态 DRAFT(0) → PUBLISHED(1) → DISABLED(2)
     * 优先级二：按更新时间倒序（默认）或正序
     * 优先级三：按接口分类在目录中的顺序（sort_order）
     * 优先级四：按接口名称升序
     *
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @param keyword    关键词
     * @param status     状态筛选
     * @param categoryId 分类ID筛选（查询该分类及其所有后代分类下的接口）
     * @param source     来源筛选
     * @param sortOrder  更新时间排序方向
     * @return 分页结果
     */
    @Override
    public Page<ApiInfoVo> getApiList(Integer pageNum, Integer pageSize, String keyword,
                                       String status, Long categoryId, String source, String sortOrder) {
        // 1. 构建分页参数
        Page<ApiInfo> page = new Page<>(
                pageNum != null ? pageNum : 1,
                pageSize != null ? pageSize : 20
        );

        // 2. 构建查询条件（逻辑删除条件由 @TableLogic 自动处理）
        LambdaQueryWrapper<ApiInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词模糊匹配
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(w -> w
                    .like(ApiInfo::getApiName, keyword)
                    .or()
                    .like(ApiInfo::getApiDescription, keyword)
            );
        }

        // 状态筛选（多值逗号分隔）
        if (status != null && !status.isBlank()) {
            List<Integer> statusList = StatusUtils.parseStatusList(status);
            if (!statusList.isEmpty()) {
                queryWrapper.in(ApiInfo::getStatus, statusList);
            }
        }

        // 分类ID筛选：查询该分类及其所有后代分类下的接口
        if (categoryId != null) {
            List<Long> categoryIds = getDescendantCategoryIds(categoryId);
            queryWrapper.in(ApiInfo::getCategoryId, categoryIds);
        }

        if (source != null && !source.isBlank()) {
            queryWrapper.eq(ApiInfo::getSource, source);
        }

        // 3. 排序：四级排序（状态→更新时间→分类目录顺序→接口名称）
        boolean sortDesc = sortOrder == null || "desc".equalsIgnoreCase(sortOrder);
        String sortDir = sortDesc ? "DESC" : "ASC";
        queryWrapper.last("ORDER BY FIELD(status, 0, 1, 2), update_time " + sortDir
                + ", (SELECT COALESCE(sort_order, 0) FROM api_category WHERE id = category_id) ASC"
                + ", api_name ASC");

        // 4. 执行分页查询
        Page<ApiInfo> apiInfoPage = page(page, queryWrapper);

        // 5. 加载所有分类用于构建分类路径
        Map<Long, ApiCategory> categoryMap = loadCategoryMap();

        // 6. 转换为 VO
        List<ApiInfoVo> voList = apiInfoPage.getRecords().stream()
                .map(api -> convertToVo(api, categoryMap))
                .collect(Collectors.toList());

        // 7. 构建分页结果
        Page<ApiInfoVo> voPage = new Page<>(apiInfoPage.getCurrent(), apiInfoPage.getSize(), apiInfoPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询接口详情
     *
     * @param id 接口ID
     * @return 接口完整信息（含 headers、requestParams、responseExample、remark）
     */
    @Override
    public ApiDetailVo getApiDetail(Long id) {
        // 1. 查询接口基本信息
        ApiInfo apiInfo = lambdaQuery()
                .eq(ApiInfo::getId, id)
                .one();
        if (apiInfo == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "接口不存在");
        }

        // 2. 加载分类映射
        Map<Long, ApiCategory> categoryMap = loadCategoryMap();

        // 3. 构建基础信息
        ApiDetailVo detailVo = convertToDetailVo(apiInfo, categoryMap);

        // 4. 查询请求头配置
        List<ApiHeader> headers = apiHeaderMapper.selectList(
                new LambdaQueryWrapper<ApiHeader>()
                        .eq(ApiHeader::getApiId, id)
                        .orderByAsc(ApiHeader::getSortOrder)
        );
        detailVo.setHeaders(headers.stream().map(this::convertToHeaderVo).collect(Collectors.toList()));

        // 5. 查询请求参数配置
        List<ApiParam> params = apiParamMapper.selectList(
                new LambdaQueryWrapper<ApiParam>()
                        .eq(ApiParam::getApiId, id)
                        .orderByAsc(ApiParam::getSortOrder)
        );
        detailVo.setRequestParams(params.stream().map(this::convertToParamVo).collect(Collectors.toList()));

        return detailVo;
    }

    /**
     * 新增注册接口
     *
     * 创建接口基本信息，同时保存请求头和请求参数配置，初始状态为 DRAFT(0)。
     *
     * @param request 新增接口请求参数
     * @return 创建的接口实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiInfo createApi(ApiCreateRequest request) {
        // 1. 校验分类是否存在
        ApiCategory category = apiCategoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "所属分类不存在");
        }

        // 2. 校验接口名称全局唯一性
        Long nameCount = lambdaQuery()
                .eq(ApiInfo::getApiName, request.getApiName())
                .count();
        if (nameCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "接口名称已存在，请使用其他名称");
        }

        // 3. 校验接口 URL 全局唯一性
        Long urlCount = lambdaQuery()
                .eq(ApiInfo::getUrl, request.getUrl())
                .count();
        if (urlCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "接口 URL 已存在，请使用其他 URL");
        }

        // 4. 创建接口基本信息
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setApiName(request.getApiName());
        apiInfo.setApiDescription(request.getApiDescription());
        apiInfo.setCategoryId(request.getCategoryId());
        apiInfo.setSource(request.getSource());
        apiInfo.setProtocol(request.getProtocol() != null ? request.getProtocol() : "HTTP");
        apiInfo.setMethod(request.getMethod());
        apiInfo.setUrl(request.getUrl());
        apiInfo.setTimeout(request.getTimeout() != null ? request.getTimeout() : 30000);
        apiInfo.setRetryCount(request.getRetryCount() != null ? request.getRetryCount() : 0);
        apiInfo.setStatus(0); // DRAFT
        apiInfo.setVersion("1.0.0");
        apiInfo.setResponseExample(request.getResponseExample());
        apiInfo.setRemark(request.getRemark());

        save(apiInfo);

        // 3. 保存请求头配置
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            List<ApiHeader> headerList = request.getHeaders().stream()
                    .map(h -> {
                        ApiHeader header = new ApiHeader();
                        header.setApiId(apiInfo.getId());
                        header.setHeaderKey(h.getKey());
                        header.setHeaderValue(h.getValue());
                        header.setRequired(Boolean.TRUE.equals(h.getRequired()) ? 1 : 0);
                        header.setDescription(h.getDescription());
                        return header;
                    })
                    .collect(Collectors.toList());
            headerList.forEach(apiHeaderMapper::insert);
        }

        // 4. 保存请求参数配置
        if (request.getRequestParams() != null && !request.getRequestParams().isEmpty()) {
            List<ApiParam> paramList = request.getRequestParams().stream()
                    .map(p -> {
                        ApiParam param = new ApiParam();
                        param.setApiId(apiInfo.getId());
                        param.setParamName(p.getParamName());
                        param.setParamType(p.getParamType());
                        param.setDataType(p.getDataType());
                        param.setRequired(Boolean.TRUE.equals(p.getRequired()) ? 1 : 0);
                        param.setDescription(p.getDescription());
                        param.setDefaultValue(p.getDefaultValue());
                        param.setExampleValue(p.getExampleValue());
                        param.setSortOrder(p.getSortOrder() != null ? p.getSortOrder() : 0);
                        param.setValidationRule(p.getValidationRule());
                        param.setMinValue(p.getMinValue());
                        param.setMaxValue(p.getMaxValue());
                        return param;
                    })
                    .collect(Collectors.toList());
            paramList.forEach(apiParamMapper::insert);
        }

        log.info("新增注册接口成功：apiName={}, id={}", request.getApiName(), apiInfo.getId());
        return apiInfo;
    }

    /**
     * 编辑接口
     *
     * 仅未发布(0)和已停用(2)状态可编辑。已停用状态下不可编辑 url（Path）字段。
     * 已发布(1)状态不可编辑。
     *
     * @param id      接口ID
     * @param request 编辑接口请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApi(Long id, ApiUpdateRequest request) {
        // 1. 校验接口是否存在
        ApiInfo apiInfo = lambdaQuery()
                .eq(ApiInfo::getId, id)
                .one();
        if (apiInfo == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "接口不存在");
        }

        // 2. 校验状态（仅未发布和已停用状态可编辑）
        if (apiInfo.getStatus() != 0 && apiInfo.getStatus() != 2) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布状态的接口不可编辑");
        }

        // 3. 已停用状态下不可修改 url（Path）字段
        if (apiInfo.getStatus() == 2 && !apiInfo.getUrl().equals(request.getUrl())) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已停用状态下不可修改 Path 字段");
        }

        // 4. 校验接口名称全局唯一性（排除自身）
        Long nameCount = lambdaQuery()
                .eq(ApiInfo::getApiName, request.getApiName())
                .ne(ApiInfo::getId, id)
                .count();
        if (nameCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "接口名称已存在，请使用其他名称");
        }

        // 5. 校验接口 URL 全局唯一性（排除自身）
        Long urlCount = lambdaQuery()
                .eq(ApiInfo::getUrl, request.getUrl())
                .ne(ApiInfo::getId, id)
                .count();
        if (urlCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "接口 URL 已存在，请使用其他 URL");
        }

        // 6. 校验分类是否存在
        ApiCategory category = apiCategoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "所属分类不存在");
        }

        // 5. 更新基本信息
        apiInfo.setApiName(request.getApiName());
        apiInfo.setApiDescription(request.getApiDescription());
        apiInfo.setCategoryId(request.getCategoryId());
        apiInfo.setSource(request.getSource());
        apiInfo.setProtocol(request.getProtocol());
        apiInfo.setMethod(request.getMethod());
        apiInfo.setUrl(request.getUrl());
        apiInfo.setTimeout(request.getTimeout() != null ? request.getTimeout() : 30000);
        apiInfo.setRetryCount(request.getRetryCount() != null ? request.getRetryCount() : 0);
        apiInfo.setResponseExample(request.getResponseExample());
        apiInfo.setRemark(request.getRemark());

        lambdaUpdate()
                .eq(ApiInfo::getId, id)
                .update(apiInfo);

        // 6. 重新保存请求头配置（先删后增）
        apiHeaderMapper.delete(new LambdaQueryWrapper<ApiHeader>().eq(ApiHeader::getApiId, id));
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            List<ApiHeader> headerList = request.getHeaders().stream()
                    .map(h -> {
                        ApiHeader header = new ApiHeader();
                        header.setApiId(id);
                        header.setHeaderKey(h.getKey());
                        header.setHeaderValue(h.getValue());
                        header.setRequired(Boolean.TRUE.equals(h.getRequired()) ? 1 : 0);
                        header.setDescription(h.getDescription());
                        return header;
                    })
                    .collect(Collectors.toList());
            headerList.forEach(apiHeaderMapper::insert);
        }

        // 7. 重新保存请求参数配置（先删后增）
        apiParamMapper.delete(new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, id));
        if (request.getRequestParams() != null && !request.getRequestParams().isEmpty()) {
            List<ApiParam> paramList = request.getRequestParams().stream()
                    .map(p -> {
                        ApiParam param = new ApiParam();
                        param.setApiId(id);
                        param.setParamName(p.getParamName());
                        param.setParamType(p.getParamType());
                        param.setDataType(p.getDataType());
                        param.setRequired(Boolean.TRUE.equals(p.getRequired()) ? 1 : 0);
                        param.setDescription(p.getDescription());
                        param.setDefaultValue(p.getDefaultValue());
                        param.setExampleValue(p.getExampleValue());
                        param.setSortOrder(p.getSortOrder() != null ? p.getSortOrder() : 0);
                        param.setValidationRule(p.getValidationRule());
                        param.setMinValue(p.getMinValue());
                        param.setMaxValue(p.getMaxValue());
                        return param;
                    })
                    .collect(Collectors.toList());
            paramList.forEach(apiParamMapper::insert);
        }

        log.info("编辑注册接口成功：id={}, apiName={}", id, request.getApiName());
    }

    /**
     * 发布接口
     *
     * 将未发布(0)或已停用(2)状态的接口发布为已发布(1)。
     *
     * @param id 接口ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishApi(Long id) {
        // 1. 校验接口是否存在
        ApiInfo apiInfo = lambdaQuery()
                .eq(ApiInfo::getId, id)
                .one();
        if (apiInfo == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "接口不存在");
        }

        // 2. 校验状态（仅未发布或已停用状态可发布）
        if (apiInfo.getStatus() != 0 && apiInfo.getStatus() != 2) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅未发布或已停用状态的接口可发布");
        }

        // 3. 更新状态
        lambdaUpdate()
                .eq(ApiInfo::getId, id)
                .set(ApiInfo::getStatus, 1)
                .update();

        log.info("发布接口成功：id={}, apiName={}", id, apiInfo.getApiName());
    }

    /**
     * 停用接口
     *
     * 将 PUBLISHED(1) 状态的接口变更为 DISABLED(2)。
     *
     * @param id 接口ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableApi(Long id) {
        // 1. 校验接口是否存在
        ApiInfo apiInfo = lambdaQuery()
                .eq(ApiInfo::getId, id)
                .one();
        if (apiInfo == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "接口不存在");
        }

        // 2. 校验状态（仅已发布状态可停用）
        if (apiInfo.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅已发布状态的接口可停用");
        }

        // 3. 更新状态
        lambdaUpdate()
                .eq(ApiInfo::getId, id)
                .set(ApiInfo::getStatus, 2)
                .update();

        log.info("停用接口成功：id={}, apiName={}", id, apiInfo.getApiName());
    }

    /**
     * 删除接口
     *
     * 仅可删除 DRAFT(0) 状态的接口（物理删除通过逻辑删除实现）。
     *
     * @param id 接口ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApi(Long id) {
        // 1. 校验接口是否存在
        ApiInfo apiInfo = lambdaQuery()
                .eq(ApiInfo::getId, id)
                .one();
        if (apiInfo == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "接口不存在");
        }

        // 2. 校验状态（仅草稿状态可删除）
        if (apiInfo.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布或已停用的接口不可删除");
        }

        // 3. 删除接口关联的请求头和参数配置
        apiHeaderMapper.delete(new LambdaQueryWrapper<ApiHeader>().eq(ApiHeader::getApiId, id));
        apiParamMapper.delete(new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, id));

        // 4. 逻辑删除接口
        removeById(id);

        log.info("删除接口成功：id={}, apiName={}", id, apiInfo.getApiName());
    }

    /**
     * 批量发布接口
     *
     * 校验规则：所选接口不能包含已发布(1)状态的接口，可包含未发布(0)和已停用(2)状态。
     *
     * @param request 批量操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选接口
        List<ApiInfo> apiList = lambdaQuery()
                .in(ApiInfo::getId, ids)
                .list();

        // 2. 校验数据完整性
        if (apiList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分接口不存在");
        }

        // 3. 校验是否包含已发布接口（未发布和已停用均可发布）
        boolean hasInvalid = apiList.stream().anyMatch(api -> api.getStatus() == 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选接口中包含已发布状态的接口，操作不合法");
        }

        // 4. 批量更新为已发布
        lambdaUpdate()
                .in(ApiInfo::getId, ids)
                .set(ApiInfo::getStatus, 1)
                .update();

        log.info("批量发布接口成功：ids={}", ids);
    }

    /**
     * 批量停用接口
     *
     * 校验规则：所选接口不能包含未发布(0)或已停用(2)状态的接口
     *
     * @param request 批量操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisable(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选接口
        List<ApiInfo> apiList = lambdaQuery()
                .in(ApiInfo::getId, ids)
                .list();

        // 2. 校验数据完整性
        if (apiList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分接口不存在");
        }

        // 3. 校验是否均为已发布状态
        boolean hasInvalid = apiList.stream().anyMatch(api -> api.getStatus() != 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选接口中包含未发布或已停用状态的接口，操作不合法");
        }

        // 4. 批量更新为已停用
        lambdaUpdate()
                .in(ApiInfo::getId, ids)
                .set(ApiInfo::getStatus, 2)
                .update();

        log.info("批量停用接口成功：ids={}", ids);
    }

    /**
     * 批量修改接口分类
     *
     * 校验规则：所选接口不能包含已发布(1)状态的接口，可包含未发布(0)和已停用(2)状态。
     *
     * @param request 批量分类请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCategory(BatchCategoryRequest request) {
        List<Long> ids = request.getIds();
        Long categoryId = request.getCategoryId();

        // 1. 校验目标分类是否存在
        ApiCategory category = apiCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "目标分类不存在");
        }

        // 2. 查询所选接口
        List<ApiInfo> apiList = lambdaQuery()
                .in(ApiInfo::getId, ids)
                .list();

        // 3. 校验数据完整性
        if (apiList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分接口不存在");
        }

        // 4. 校验是否包含已发布接口（未发布和已停用均可分类）
        boolean hasPublished = apiList.stream().anyMatch(api -> api.getStatus() == 1);
        if (hasPublished) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选接口中包含已发布状态的接口，操作不合法");
        }

        // 5. 批量更新分类
        lambdaUpdate()
                .in(ApiInfo::getId, ids)
                .set(ApiInfo::getCategoryId, categoryId)
                .update();

        log.info("批量修改接口分类成功：ids={}, categoryId={}", ids, categoryId);
    }

    /**
     * 接口测试调用
     *
     * 根据接口配置通过 RestTemplate 发起 HTTP 请求，返回调用结果。
     * 不限制接口状态，任何状态的接口均可测试。
     *
     * @param id      接口ID
     * @param request 测试调用请求（临时参数值）
     * @return 测试调用结果
     */
    @Override
    public ApiTestResultVo testApi(Long id, ApiTestRequest request) {
        // 1. 查询接口信息
        ApiInfo apiInfo = lambdaQuery()
                .eq(ApiInfo::getId, id)
                .one();
        if (apiInfo == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "接口不存在");
        }

        // 2. 构建测试结果
        ApiTestResultVo result = new ApiTestResultVo();
        long startTime = System.currentTimeMillis();

        try {
            // 3. 构建请求头
            HttpHeaders headers = new HttpHeaders();
            List<ApiHeader> headerConfigs = apiHeaderMapper.selectList(
                    new LambdaQueryWrapper<ApiHeader>().eq(ApiHeader::getApiId, id)
            );
            for (ApiHeader h : headerConfigs) {
                headers.set(h.getHeaderKey(), h.getHeaderValue());
            }

            // 4. 处理URL中的路径参数
            String url = apiInfo.getUrl();
            Map<String, Object> paramValues = request.getParamValues() != null
                    ? request.getParamValues() : new HashMap<>();

            // 替换路径参数 {xxx}
            for (Map.Entry<String, Object> entry : paramValues.entrySet()) {
                url = url.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }

            // 5. 发起HTTP请求
            RestTemplate restTemplate = new RestTemplate();
            HttpMethod httpMethod = HttpMethod.valueOf(apiInfo.getMethod().toUpperCase());
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, httpMethod, entity, String.class, paramValues
            );

            // 6. 构建成功结果
            long endTime = System.currentTimeMillis();
            result.setSuccess(true);
            result.setStatusCode(response.getStatusCode().value());
            result.setResponseTime(endTime - startTime);
            result.setResponseBody(response.getBody());

            Map<String, String> responseHeaders = new HashMap<>();
            response.getHeaders().forEach((key, values) -> {
                if (key != null && !values.isEmpty()) {
                    responseHeaders.put(key, String.join(", ", values));
                }
            });
            result.setResponseHeaders(responseHeaders);

        } catch (Exception e) {
            // 7. 构建失败结果
            long endTime = System.currentTimeMillis();
            result.setSuccess(false);
            result.setResponseTime(endTime - startTime);
            result.setErrorMessage(e.getMessage());
            log.warn("接口测试调用失败：id={}, error={}", id, e.getMessage());
        }

        return result;
    }

    // ==================== 私有工具方法 ====================

    /**
     * 加载全部分类并构建 id → entity 映射
     */
    private Map<Long, ApiCategory> loadCategoryMap() {
        List<ApiCategory> categories = apiCategoryMapper.selectList(null);
        return categories.stream()
                .collect(Collectors.toMap(ApiCategory::getId, c -> c, (a, b) -> a));
    }

    /**
     * 递归构建分类路径（如 "电商数据 / 订单接口"）
     */
    private String buildCategoryPath(Long categoryId, Map<Long, ApiCategory> categoryMap) {
        List<String> names = new ArrayList<>();
        Long currentId = categoryId;
        while (currentId != null && currentId != 0L) {
            ApiCategory cat = categoryMap.get(currentId);
            if (cat == null) break;
            names.add(0, cat.getName());
            currentId = cat.getParentId();
        }
        return String.join(" / ", names);
    }

    /**
     * ApiInfo → ApiInfoVo 转换
     */
    private ApiInfoVo convertToVo(ApiInfo api, Map<Long, ApiCategory> categoryMap) {
        ApiInfoVo vo = new ApiInfoVo();
        vo.setId(api.getId());
        vo.setApiName(api.getApiName());
        vo.setApiDescription(api.getApiDescription());
        vo.setApiCategory(buildCategoryPath(api.getCategoryId(), categoryMap));
        vo.setCategoryId(api.getCategoryId());
        vo.setSource(api.getSource());
        vo.setProtocol(api.getProtocol());
        vo.setMethod(api.getMethod());
        vo.setUrl(api.getUrl());
        vo.setStatus(api.getStatus());
        vo.setTimeout(api.getTimeout());
        vo.setRetryCount(api.getRetryCount());
        vo.setVersion(api.getVersion());
        vo.setCreateTime(api.getCreateTime());
        vo.setUpdateTime(api.getUpdateTime());
        vo.setCreateBy(api.getCreateBy());
        vo.setUpdateBy(api.getUpdateBy());
        return vo;
    }

    /**
     * ApiInfo → ApiDetailVo 转换（基础信息）
     */
    private ApiDetailVo convertToDetailVo(ApiInfo api, Map<Long, ApiCategory> categoryMap) {
        ApiDetailVo vo = new ApiDetailVo();
        vo.setId(api.getId());
        vo.setApiName(api.getApiName());
        vo.setApiDescription(api.getApiDescription());
        vo.setApiCategory(buildCategoryPath(api.getCategoryId(), categoryMap));
        vo.setCategoryId(api.getCategoryId());
        vo.setSource(api.getSource());
        vo.setProtocol(api.getProtocol());
        vo.setMethod(api.getMethod());
        vo.setUrl(api.getUrl());
        vo.setTimeout(api.getTimeout());
        vo.setRetryCount(api.getRetryCount());
        vo.setStatus(api.getStatus());
        vo.setVersion(api.getVersion());
        vo.setResponseExample(api.getResponseExample());
        vo.setRemark(api.getRemark());
        vo.setCreateTime(api.getCreateTime());
        vo.setUpdateTime(api.getUpdateTime());
        vo.setCreateBy(api.getCreateBy());
        vo.setUpdateBy(api.getUpdateBy());
        return vo;
    }

    /**
     * ApiHeader → HeaderConfigVo 转换
     */
    private HeaderConfigVo convertToHeaderVo(ApiHeader header) {
        HeaderConfigVo vo = new HeaderConfigVo();
        vo.setKey(header.getHeaderKey());
        vo.setValue(header.getHeaderValue());
        vo.setRequired(header.getRequired() == 1);
        vo.setDescription(header.getDescription());
        return vo;
    }

    /**
     * ApiParam → ParamConfigVo 转换
     */
    private ParamConfigVo convertToParamVo(ApiParam param) {
        ParamConfigVo vo = new ParamConfigVo();
        vo.setId(param.getId());
        vo.setParamName(param.getParamName());
        vo.setParamType(param.getParamType());
        vo.setDataType(param.getDataType());
        vo.setRequired(param.getRequired() == 1);
        vo.setDescription(param.getDescription());
        vo.setDefaultValue(param.getDefaultValue());
        vo.setExampleValue(param.getExampleValue());
        vo.setSortOrder(param.getSortOrder());
        vo.setValidationRule(param.getValidationRule());
        vo.setMinValue(param.getMinValue());
        vo.setMaxValue(param.getMaxValue());
        return vo;
    }

    /**
     * 递归获取指定分类及其所有后代分类的 ID 列表
     *
     * 全量加载分类数据后，在内存中按 parentId 分组，再通过广度优先遍历
     * 获取所有后代分类的 ID，用于查询该分类及其所有后代分类下的接口。
     *
     * @param categoryId 父分类ID
     * @return 包含自身及所有后代分类的 ID 列表
     */
    private List<Long> getDescendantCategoryIds(Long categoryId) {
        List<Long> result = new ArrayList<>();
        result.add(categoryId);

        // 全量加载分类（@TableLogic 自动过滤已删除）
        List<ApiCategory> allCategories = apiCategoryMapper.selectList(null);
        // 按 parentId 分组
        Map<Long, List<ApiCategory>> childrenMap = allCategories.stream()
                .collect(Collectors.groupingBy(ApiCategory::getParentId));

        // 广度优先遍历查找所有后代
        List<Long> queue = new ArrayList<>(result);
        while (!queue.isEmpty()) {
            Long current = queue.remove(0);
            List<ApiCategory> children = childrenMap.getOrDefault(current, new ArrayList<>());
            for (ApiCategory child : children) {
                result.add(child.getId());
                queue.add(child.getId());
            }
        }

        return result;
    }
}
