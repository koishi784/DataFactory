package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.codetable.CodeItemCreateRequest;
import com.datafactory.common.model.dto.codetable.CodeItemUpdateRequest;
import com.datafactory.common.model.dto.codetable.CodeTableCreateRequest;
import com.datafactory.common.model.dto.codetable.CodeTableUpdateRequest;
import com.datafactory.common.model.vo.codetable.CodeItemVo;
import com.datafactory.common.model.vo.codetable.CodeTableDetailVo;
import com.datafactory.common.model.vo.codetable.CodeTableListVo;
import com.datafactory.common.utils.StatusUtils;
import com.datafactory.core.domain.entity.CodeItem;
import com.datafactory.core.domain.entity.CodeTable;
import com.datafactory.core.domain.mapper.CodeItemMapper;
import com.datafactory.core.domain.mapper.CodeTableMapper;
import com.datafactory.core.service.ICodeTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 码表管理服务实现类
 *
 * 实现码表 CRUD、状态管理、批量操作，以及码值的增删改查等完整业务逻辑。
 * 码表编号由系统自动生成（格式 MZB + 5 位数字），基于保存后的 ID 生成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ICodeTableServiceImpl extends ServiceImpl<CodeTableMapper, CodeTable> implements ICodeTableService {

    private final CodeItemMapper codeItemMapper;

    /**
     * 分页查询码表列表
     *
     * 支持多条件筛选（关键词、状态），排序规则：
     * 优先级一：按状态 DRAFT(0) → PUBLISHED(1) → DISABLED(2)
     * 优先级二：按更新时间倒序排列
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，匹配码表名称、码表编号
     * @param status   状态筛选，多值用逗号分隔
     * @return 分页结果
     */
    @Override
    public Page<CodeTableListVo> getCodeTableList(Integer pageNum, Integer pageSize, String keyword, String status) {
        // 1. 构建分页参数
        Page<CodeTable> page = new Page<>(
                pageNum != null ? pageNum : 1,
                pageSize != null ? pageSize : 20
        );

        // 2. 构建查询条件
        LambdaQueryWrapper<CodeTable> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词模糊匹配（码表名称、码表编号）
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(w -> w
                    .like(CodeTable::getTableName, keyword)
                    .or()
                    .like(CodeTable::getTableCode, keyword)
            );
        }

        // 状态筛选（多值逗号分隔）
        if (status != null && !status.isBlank()) {
            List<Integer> statusList = StatusUtils.parseStatusList(status);
            if (!statusList.isEmpty()) {
                queryWrapper.in(CodeTable::getStatus, statusList);
            }
        }

        // 3. 排序：状态优先，再按更新时间倒序
        queryWrapper.last("ORDER BY FIELD(status, 0, 1, 2), update_time DESC");

        // 4. 执行分页查询
        Page<CodeTable> entityPage = page(page, queryWrapper);

        // 5. 转换为 VO
        List<CodeTableListVo> voList = entityPage.getRecords().stream()
                .map(this::convertToListVo)
                .collect(Collectors.toList());

        // 6. 构建分页结果
        Page<CodeTableListVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询码表详情（含码值列表）
     *
     * @param id 码表ID
     * @return 码表详情
     */
    @Override
    public CodeTableDetailVo getCodeTableDetail(Long id) {
        CodeTable codeTable = lambdaQuery()
                .eq(CodeTable::getId, id)
                .one();
        if (codeTable == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码表不存在");
        }

        CodeTableDetailVo vo = convertToDetailVo(codeTable);

        // 查询码值列表
        List<CodeItem> items = codeItemMapper.selectList(
                new LambdaQueryWrapper<CodeItem>()
                        .eq(CodeItem::getTableId, id)
                        .orderByAsc(CodeItem::getSortOrder)
        );
        vo.setItems(items.stream().map(this::convertToItemVo).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 查询码值列表
     *
     * @param tableId 码表ID
     * @return 码值列表
     */
    @Override
    public List<CodeItemVo> getCodeItems(Long tableId) {
        // 1. 校验码表是否存在
        CodeTable codeTable = lambdaQuery()
                .eq(CodeTable::getId, tableId)
                .one();
        if (codeTable == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码表不存在");
        }

        // 2. 查询码值列表
        List<CodeItem> items = codeItemMapper.selectList(
                new LambdaQueryWrapper<CodeItem>()
                        .eq(CodeItem::getTableId, tableId)
                        .orderByAsc(CodeItem::getSortOrder)
        );

        return items.stream().map(this::convertToItemVo).collect(Collectors.toList());
    }

    /**
     * 新增码表
     *
     * 码表编号由系统自动生成（格式 MZB + 5 位数字），创建后状态为 DRAFT(0)。
     * 支持同时传入初始码值列表。
     *
     * @param request 新增码表请求参数
     * @return 创建的码表详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CodeTableDetailVo createCodeTable(CodeTableCreateRequest request) {
        // 1. 校验码表名称唯一性
        Long nameCount = lambdaQuery()
                .eq(CodeTable::getTableName, request.getTableName())
                .count();
        if (nameCount > 0) {
            throw new BusinessException(100409, "码表名称已存在");
        }

        // 2. 创建并保存码表（先设空占位值，保存后生成正式编号）
        CodeTable codeTable = new CodeTable();
        codeTable.setTableName(request.getTableName());
        codeTable.setDescription(request.getDescription());
        codeTable.setTableCode(""); // 保存后立即更新为正式编号
        codeTable.setStatus(0); // DRAFT
        save(codeTable);

        // 3. 生成码表编号（MZB + ID 后 5 位）
        codeTable.setTableCode(generateTableCode(codeTable.getId()));
        updateById(codeTable);

        // 4. 保存初始码值列表（如果有）
        List<CodeItemVo> itemVos = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (int i = 0; i < request.getItems().size(); i++) {
                CodeItemCreateRequest itemReq = request.getItems().get(i);
                // 校验码值唯一性
                validateCodeItemUniqueness(codeTable.getId(), itemReq.getCode(), itemReq.getName(), null);

                CodeItem item = new CodeItem();
                item.setTableId(codeTable.getId());
                item.setCode(itemReq.getCode().trim());
                item.setName(itemReq.getName().trim());
                item.setValue(itemReq.getValue());
                item.setSortOrder(itemReq.getSortOrder() != null ? itemReq.getSortOrder() : i + 1);
                item.setParentCode(itemReq.getParentCode());
                item.setDescription(itemReq.getDescription());
                item.setStatus(1); // 默认启用
                codeItemMapper.insert(item);

                itemVos.add(convertToItemVo(item));
            }
        }

        log.info("新增码表成功：tableName={}, tableCode={}, id={}, items={}",
                request.getTableName(), codeTable.getTableCode(), codeTable.getId(), itemVos.size());

        CodeTableDetailVo vo = convertToDetailVo(codeTable);
        vo.setItems(itemVos);
        return vo;
    }

    /**
     * 编辑码表
     *
     * 仅未发布(0)和已停用(2)状态可编辑。码表编号不可修改。
     *
     * @param id      码表ID
     * @param request 编辑码表请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCodeTable(Long id, CodeTableUpdateRequest request) {
        // 1. 校验码表是否存在
        CodeTable codeTable = lambdaQuery()
                .eq(CodeTable::getId, id)
                .one();
        if (codeTable == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码表不存在");
        }

        // 2. 校验状态（仅未发布和已停用可编辑）
        if (codeTable.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布状态的码表不可编辑");
        }

        // 3. 更新字段（tableCode 不可修改）
        codeTable.setTableName(request.getTableName());
        codeTable.setDescription(request.getDescription());
        updateById(codeTable);

        log.info("编辑码表成功：id={}, tableName={}", id, request.getTableName());
    }

    /**
     * 新增码值
     *
     * @param tableId 码表ID
     * @param request 新增码值请求参数
     * @return 创建的码值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CodeItemVo createCodeItem(Long tableId, CodeItemCreateRequest request) {
        // 1. 校验码表是否存在
        CodeTable codeTable = lambdaQuery()
                .eq(CodeTable::getId, tableId)
                .one();
        if (codeTable == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码表不存在");
        }

        // 2. 校验码值 code 和 name 唯一性
        validateCodeItemUniqueness(tableId, request.getCode(), request.getName(), null);

        // 3. 创建码值
        CodeItem item = new CodeItem();
        item.setTableId(tableId);
        item.setCode(request.getCode().trim());
        item.setName(request.getName().trim());
        item.setValue(request.getValue());
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        item.setParentCode(request.getParentCode());
        item.setDescription(request.getDescription());
        item.setStatus(1); // 默认启用
        codeItemMapper.insert(item);

        log.info("新增码值成功：tableId={}, code={}, name={}, id={}", tableId, item.getCode(), item.getName(), item.getId());
        return convertToItemVo(item);
    }

    /**
     * 更新码值
     *
     * @param tableId 码表ID
     * @param itemId  码值ID
     * @param request 更新码值请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCodeItem(Long tableId, Long itemId, CodeItemUpdateRequest request) {
        // 1. 校验码值是否存在且属于该码表
        CodeItem item = codeItemMapper.selectOne(
                new LambdaQueryWrapper<CodeItem>()
                        .eq(CodeItem::getId, itemId)
                        .eq(CodeItem::getTableId, tableId)
        );
        if (item == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码值不存在");
        }

        // 2. 校验 code 和 name 唯一性（排除自身）
        validateCodeItemUniqueness(tableId, request.getCode(), request.getName(), itemId);

        // 3. 更新码值
        item.setCode(request.getCode().trim());
        item.setName(request.getName().trim());
        item.setValue(request.getValue());
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : item.getSortOrder());
        item.setParentCode(request.getParentCode());
        item.setDescription(request.getDescription());
        codeItemMapper.updateById(item);

        log.info("更新码值成功：tableId={}, itemId={}, code={}", tableId, itemId, item.getCode());
    }

    /**
     * 删除码值
     *
     * @param tableId 码表ID
     * @param itemId  码值ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCodeItem(Long tableId, Long itemId) {
        // 1. 校验码值是否存在且属于该码表
        CodeItem item = codeItemMapper.selectOne(
                new LambdaQueryWrapper<CodeItem>()
                        .eq(CodeItem::getId, itemId)
                        .eq(CodeItem::getTableId, tableId)
        );
        if (item == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码值不存在");
        }

        // 2. 逻辑删除
        codeItemMapper.deleteById(itemId);
        log.info("删除码值成功：tableId={}, itemId={}, code={}", tableId, itemId, item.getCode());
    }

    /**
     * 发布码表
     *
     * 将未发布(0)或已停用(2)状态发布为已发布(1)。
     *
     * @param id 码表ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishCodeTable(Long id) {
        CodeTable codeTable = lambdaQuery()
                .eq(CodeTable::getId, id)
                .one();
        if (codeTable == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码表不存在");
        }
        if (codeTable.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "码表已发布，无需重复发布");
        }

        lambdaUpdate()
                .eq(CodeTable::getId, id)
                .set(CodeTable::getStatus, 1)
                .update();

        log.info("发布码表成功：id={}, tableName={}", id, codeTable.getTableName());
    }

    /**
     * 停用码表
     *
     * 将已发布(1)状态变更为已停用(2)。
     *
     * @param id 码表ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableCodeTable(Long id) {
        CodeTable codeTable = lambdaQuery()
                .eq(CodeTable::getId, id)
                .one();
        if (codeTable == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码表不存在");
        }
        if (codeTable.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅已发布状态的码表可停用");
        }

        lambdaUpdate()
                .eq(CodeTable::getId, id)
                .set(CodeTable::getStatus, 2)
                .update();

        log.info("停用码表成功：id={}, tableName={}", id, codeTable.getTableName());
    }

    /**
     * 删除码表
     *
     * 仅可删除 DRAFT(0) 状态的码表（逻辑删除）。
     *
     * @param id 码表ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCodeTable(Long id) {
        CodeTable codeTable = lambdaQuery()
                .eq(CodeTable::getId, id)
                .one();
        if (codeTable == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "码表不存在");
        }
        if (codeTable.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布或已停用的码表不可删除");
        }

        // 删除关联的码值
        codeItemMapper.delete(
                new LambdaQueryWrapper<CodeItem>()
                        .eq(CodeItem::getTableId, id)
        );

        removeById(id);
        log.info("删除码表成功：id={}, tableName={}", id, codeTable.getTableName());
    }

    /**
     * 批量发布码表
     *
     * 校验规则：所选码表须全部为未发布(0)或已停用(2)状态，不能包含已发布(1)状态。
     *
     * @param request 批量操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选码表
        List<CodeTable> tableList = lambdaQuery()
                .in(CodeTable::getId, ids)
                .list();

        // 2. 校验数据完整性
        if (tableList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分码表不存在");
        }

        // 3. 校验是否包含已发布码表
        boolean hasPublished = tableList.stream().anyMatch(t -> t.getStatus() == 1);
        if (hasPublished) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选码表中包含已发布状态的数据，操作不合法");
        }

        // 4. 批量更新为已发布
        lambdaUpdate()
                .in(CodeTable::getId, ids)
                .set(CodeTable::getStatus, 1)
                .update();

        log.info("批量发布码表成功：ids={}", ids);
    }

    /**
     * 批量停用码表
     *
     * 校验规则：所选码表不能包含未发布(0)或已停用(2)状态的记录。
     *
     * @param request 批量操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisable(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选码表
        List<CodeTable> tableList = lambdaQuery()
                .in(CodeTable::getId, ids)
                .list();

        // 2. 校验数据完整性
        if (tableList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分码表不存在");
        }

        // 3. 校验是否均为已发布状态
        boolean hasInvalid = tableList.stream().anyMatch(t -> t.getStatus() != 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选码表中包含未发布或已停用状态的数据，操作不合法");
        }

        // 4. 批量更新为已停用
        lambdaUpdate()
                .in(CodeTable::getId, ids)
                .set(CodeTable::getStatus, 2)
                .update();

        log.info("批量停用码表成功：ids={}", ids);
    }

    /**
     * 根据编码查询码表
     *
     * @param tableCode 码表编号
     * @return 码表实体，不存在返回 null
     */
    @Override
    public CodeTable getByTableCode(String tableCode) {
        return lambdaQuery()
                .eq(CodeTable::getTableCode, tableCode)
                .one();
    }

    // ==================== 私有工具方法 ====================

    /**
     * 生成码表编号
     *
     * 格式为 MZB + 5 位数字（基于 ID 后 5 位补零），如 MZB00001。
     *
     * @param id 码表ID
     * @return 码表编号字符串
     */
    private String generateTableCode(Long id) {
        return "MZB" + String.format("%05d", id % 100000);
    }

    /**
     * 校验码值 code 和 name 在同一码表内的唯一性
     *
     * @param tableId    码表ID
     * @param code       编码取值
     * @param name       编码中文名称
     * @param excludeId  排除的码值ID（编辑时排除自身）
     */
    private void validateCodeItemUniqueness(Long tableId, String code, String name, Long excludeId) {
        // 校验 code 不为空或全空格
        if (code == null || code.isBlank()) {
            throw new BusinessException(StatusCode.VALIDATION_FAILED, "编码取值不能为空或全空格");
        }
        // 校验 name 不为空或全空格
        if (name == null || name.isBlank()) {
            throw new BusinessException(StatusCode.VALIDATION_FAILED, "编码中文名称不能为空或全空格");
        }
        // 校验 name 仅支持中文及大小写英文字母
        if (!name.trim().matches("^[a-zA-Z\u4e00-\u9fa5]+$")) {
            throw new BusinessException(StatusCode.VALIDATION_FAILED, "编码中文名称仅支持中文及大小写英文字母");
        }

        // 校验 code 唯一性
        LambdaQueryWrapper<CodeItem> codeWrapper = new LambdaQueryWrapper<CodeItem>()
                .eq(CodeItem::getTableId, tableId)
                .eq(CodeItem::getCode, code.trim());
        if (excludeId != null) {
            codeWrapper.ne(CodeItem::getId, excludeId);
        }
        Long codeCount = codeItemMapper.selectCount(codeWrapper);
        if (codeCount > 0) {
            throw new BusinessException(100409, "该码表下编码取值 '" + code.trim() + "' 已存在");
        }

        // 校验 name 唯一性
        LambdaQueryWrapper<CodeItem> nameWrapper = new LambdaQueryWrapper<CodeItem>()
                .eq(CodeItem::getTableId, tableId)
                .eq(CodeItem::getName, name.trim());
        if (excludeId != null) {
            nameWrapper.ne(CodeItem::getId, excludeId);
        }
        Long nameCount = codeItemMapper.selectCount(nameWrapper);
        if (nameCount > 0) {
            throw new BusinessException(100409, "该码表下编码中文名称 '" + name.trim() + "' 已存在");
        }
    }

    /**
     * CodeTable → CodeTableListVo 转换
     */
    private CodeTableListVo convertToListVo(CodeTable entity) {
        CodeTableListVo vo = new CodeTableListVo();
        vo.setId(entity.getId());
        vo.setTableName(entity.getTableName());
        vo.setTableCode(entity.getTableCode());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 查询码值数量
        Long itemCount = codeItemMapper.selectCount(
                new LambdaQueryWrapper<CodeItem>()
                        .eq(CodeItem::getTableId, entity.getId())
        );
        vo.setCodeItemCount(itemCount.intValue());

        return vo;
    }

    /**
     * CodeTable → CodeTableDetailVo 转换
     */
    private CodeTableDetailVo convertToDetailVo(CodeTable entity) {
        CodeTableDetailVo vo = new CodeTableDetailVo();
        vo.setId(entity.getId());
        vo.setTableName(entity.getTableName());
        vo.setTableCode(entity.getTableCode());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 查询码值数量
        Long itemCount = codeItemMapper.selectCount(
                new LambdaQueryWrapper<CodeItem>()
                        .eq(CodeItem::getTableId, entity.getId())
        );
        vo.setCodeItemCount(itemCount.intValue());

        return vo;
    }

    /**
     * CodeItem → CodeItemVo 转换
     */
    private CodeItemVo convertToItemVo(CodeItem entity) {
        CodeItemVo vo = new CodeItemVo();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setValue(entity.getValue());
        vo.setSortOrder(entity.getSortOrder());
        vo.setParentCode(entity.getParentCode());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        return vo;
    }
}
