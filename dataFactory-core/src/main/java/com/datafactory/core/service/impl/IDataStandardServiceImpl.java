package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.datastandard.DataStandardCreateRequest;
import com.datafactory.common.model.dto.datastandard.DataStandardUpdateRequest;
import com.datafactory.common.model.vo.datastandard.DataStandardDetailVo;
import com.datafactory.common.model.vo.datastandard.DataStandardListVo;
import com.datafactory.common.utils.StatusUtils;
import com.datafactory.core.domain.entity.CodeTable;
import com.datafactory.core.domain.entity.DataStandard;
import com.datafactory.core.domain.mapper.DataStandardMapper;
import com.datafactory.core.service.ICodeTableService;
import com.datafactory.core.service.IDataStandardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据标准服务实现类
 *
 * 实现数据标准的 CRUD、状态管理、批量操作、数据类型校验等完整业务逻辑。
 * 标准编号由系统自动生成（格式 BZ + 5 位数字），基于保存后的 ID 生成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IDataStandardServiceImpl extends ServiceImpl<DataStandardMapper, DataStandard> implements IDataStandardService {

    private final ICodeTableService codeTableService;

    /**
     * 分页查询数据标准列表
     *
     * 支持多条件筛选（关键词、状态、数据类型），排序规则：
     * 优先级一：按状态 DRAFT(0) → PUBLISHED(1) → DISABLED(2)
     * 优先级二：按更新时间倒序排列
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，模糊匹配中文名称、英文名称、标准编号
     * @param status   状态筛选，多值用逗号分隔
     * @param dataType 数据类型筛选
     * @return 分页结果
     */
    @Override
    public Page<DataStandardListVo> getStandardList(Integer pageNum, Integer pageSize, String keyword,
                                                     String status, String dataType) {
        // 1. 构建分页参数
        Page<DataStandard> page = new Page<>(
                pageNum != null ? pageNum : 1,
                pageSize != null ? pageSize : 20
        );

        // 2. 构建查询条件
        LambdaQueryWrapper<DataStandard> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词模糊匹配（中文名称、英文名称、标准编号）
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(w -> w
                    .like(DataStandard::getName, keyword)
                    .or()
                    .like(DataStandard::getEnglishName, keyword)
                    .or()
                    .like(DataStandard::getStandardCode, keyword)
            );
        }

        // 状态筛选（多值逗号分隔）
        if (status != null && !status.isBlank()) {
            List<Integer> statusList = StatusUtils.parseStatusList(status);
            if (!statusList.isEmpty()) {
                queryWrapper.in(DataStandard::getStatus, statusList);
            }
        }

        // 数据类型筛选
        if (dataType != null && !dataType.isBlank()) {
            queryWrapper.eq(DataStandard::getDataType, dataType);
        }

        // 3. 排序：状态优先（DRAFT=0 → PUBLISHED=1 → DISABLED=2），再按更新时间倒序
        queryWrapper.last("ORDER BY FIELD(status, 0, 1, 2), update_time DESC");

        // 4. 执行分页查询
        Page<DataStandard> entityPage = page(page, queryWrapper);

        // 5. 转换为 VO
        List<DataStandardListVo> voList = entityPage.getRecords().stream()
                .map(this::convertToListVo)
                .collect(Collectors.toList());

        // 6. 构建分页结果
        Page<DataStandardListVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询数据标准详情
     *
     * @param id 标准ID
     * @return 数据标准详情
     */
    @Override
    public DataStandardDetailVo getStandardDetail(Long id) {
        DataStandard standard = lambdaQuery()
                .eq(DataStandard::getId, id)
                .one();
        if (standard == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据标准不存在");
        }
        return convertToDetailVo(standard);
    }

    /**
     * 新增数据标准
     *
     * 标准编号由系统自动生成（格式 BZ + 5 位数字，基于 ID 生成），创建后状态为 DRAFT(0)。
     * 根据数据类型校验字段合法性（各类型可填/不可填字段）。
     *
     * @param request 新增数据标准请求参数
     * @return 创建的数据标准详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataStandardDetailVo createStandard(DataStandardCreateRequest request) {
        // 1. 数据类型校验
        validateDataTypeFields(request.getDataType(), request.getLength(), request.getPrecision(),
                request.getRangeMin(), request.getRangeMax(), request.getEnumRange(), true);

        // 2. 枚举范围校验（预留注入点）
        validateEnumRange(request.getEnumRange());

        // 3. 创建实体并保存
        DataStandard standard = new DataStandard();
        standard.setName(request.getName());
        standard.setEnglishName(request.getEnglishName());
        standard.setDataType(request.getDataType());
        standard.setSourceOrganization(request.getSourceOrganization());
        standard.setLength(request.getLength());
        standard.setPrecision(request.getPrecision());
        standard.setDefaultValue(request.getDefaultValue());
        standard.setRangeMin(request.getRangeMin());
        standard.setRangeMax(request.getRangeMax());
        standard.setEnumRange(request.getEnumRange());
        standard.setNullable(request.getNullable() != null ? request.getNullable() : 0);
        standard.setDescription(request.getDescription());
        standard.setStandardCode(""); // 保存后立即更新为正式编号
        standard.setStatus(0); // DRAFT

        save(standard);

        // 4. 生成标准编号（BZ + ID 后 5 位）
        standard.setStandardCode(generateStandardCode(standard.getId()));
        updateById(standard);

        log.info("新增数据标准成功：name={}, standardCode={}, id={}",
                request.getName(), standard.getStandardCode(), standard.getId());
        return convertToDetailVo(standard);
    }

    /**
     * 编辑数据标准
     *
     * 仅未发布(0)和已停用(2)状态可编辑。标准编号不可修改。
     *
     * @param id      标准ID
     * @param request 编辑数据标准请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStandard(Long id, DataStandardUpdateRequest request) {
        // 1. 校验数据标准是否存在
        DataStandard standard = lambdaQuery()
                .eq(DataStandard::getId, id)
                .one();
        if (standard == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据标准不存在");
        }

        // 2. 校验状态（仅未发布和已停用可编辑）
        if (standard.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布状态的数据标准不可编辑");
        }

        // 3. 数据类型校验
        validateDataTypeFields(request.getDataType(), request.getLength(), request.getPrecision(),
                request.getRangeMin(), request.getRangeMax(), request.getEnumRange(), true);

        // 4. 枚举范围校验（预留注入点）
        validateEnumRange(request.getEnumRange());

        // 5. 更新字段（standardCode 不可修改）
        standard.setName(request.getName());
        standard.setEnglishName(request.getEnglishName());
        standard.setDataType(request.getDataType());
        standard.setSourceOrganization(request.getSourceOrganization());
        standard.setLength(request.getLength());
        standard.setPrecision(request.getPrecision());
        standard.setDefaultValue(request.getDefaultValue());
        standard.setRangeMin(request.getRangeMin());
        standard.setRangeMax(request.getRangeMax());
        standard.setEnumRange(request.getEnumRange());
        standard.setNullable(request.getNullable() != null ? request.getNullable() : 0);
        standard.setDescription(request.getDescription());

        updateById(standard);

        log.info("编辑数据标准成功：id={}, name={}", id, request.getName());
    }

    /**
     * 发布数据标准
     *
     * 将未发布(0)或已停用(2)状态的标准发布为已发布(1)。
     *
     * @param id 标准ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishStandard(Long id) {
        DataStandard standard = lambdaQuery()
                .eq(DataStandard::getId, id)
                .one();
        if (standard == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据标准不存在");
        }
        if (standard.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "数据标准已发布，无需重复发布");
        }

        lambdaUpdate()
                .eq(DataStandard::getId, id)
                .set(DataStandard::getStatus, 1)
                .update();

        log.info("发布数据标准成功：id={}, name={}", id, standard.getName());
    }

    /**
     * 停用数据标准
     *
     * 将已发布(1)状态的标准变更为已停用(2)。
     *
     * @param id 标准ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableStandard(Long id) {
        DataStandard standard = lambdaQuery()
                .eq(DataStandard::getId, id)
                .one();
        if (standard == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据标准不存在");
        }
        if (standard.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅已发布状态的数据标准可停用");
        }

        lambdaUpdate()
                .eq(DataStandard::getId, id)
                .set(DataStandard::getStatus, 2)
                .update();

        log.info("停用数据标准成功：id={}, name={}", id, standard.getName());
    }

    /**
     * 删除数据标准
     *
     * 仅可删除 DRAFT(0) 状态的标准（逻辑删除）。
     *
     * @param id 标准ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStandard(Long id) {
        DataStandard standard = lambdaQuery()
                .eq(DataStandard::getId, id)
                .one();
        if (standard == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据标准不存在");
        }
        if (standard.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布或已停用的数据标准不可删除");
        }

        removeById(id);
        log.info("删除数据标准成功：id={}, name={}", id, standard.getName());
    }

    /**
     * 批量发布数据标准
     *
     * 校验规则：所选标准须全部为未发布(0)或已停用(2)状态，不能包含已发布(1)状态。
     *
     * @param request 批量操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选数据标准
        List<DataStandard> standardList = lambdaQuery()
                .in(DataStandard::getId, ids)
                .list();

        // 2. 校验数据完整性
        if (standardList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分数据标准不存在");
        }

        // 3. 校验是否包含已发布标准
        boolean hasPublished = standardList.stream().anyMatch(s -> s.getStatus() == 1);
        if (hasPublished) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选数据标准中包含已发布状态的数据，操作不合法");
        }

        // 4. 批量更新为已发布
        lambdaUpdate()
                .in(DataStandard::getId, ids)
                .set(DataStandard::getStatus, 1)
                .update();

        log.info("批量发布数据标准成功：ids={}", ids);
    }

    /**
     * 批量停用数据标准
     *
     * 校验规则：所选标准不能包含未发布(0)或已停用(2)状态的记录。
     *
     * @param request 批量操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisable(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选数据标准
        List<DataStandard> standardList = lambdaQuery()
                .in(DataStandard::getId, ids)
                .list();

        // 2. 校验数据完整性
        if (standardList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分数据标准不存在");
        }

        // 3. 校验是否均为已发布状态
        boolean hasInvalid = standardList.stream().anyMatch(s -> s.getStatus() != 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选数据标准中包含未发布或已停用状态的数据，操作不合法");
        }

        // 4. 批量更新为已停用
        lambdaUpdate()
                .in(DataStandard::getId, ids)
                .set(DataStandard::getStatus, 2)
                .update();

        log.info("批量停用数据标准成功：ids={}", ids);
    }

    // ==================== 私有工具方法 ====================

    /**
     * 生成标准编号
     *
     * 格式为 BZ + 5 位数字（基于 ID 后 5 位补零），如 BZ00001。
     *
     * @param id 数据标准ID
     * @return 标准编号字符串
     */
    private String generateStandardCode(Long id) {
        return "BZ" + String.format("%05d", id % 100000);
    }

    /**
     * 校验枚举范围
     *
     * 校验 enumRange 引用的码表编码必须存在且状态为已发布(1)。
     *
     * @param enumRange 枚举范围（码表编码）
     */
    private void validateEnumRange(String enumRange) {
        if (enumRange != null && !enumRange.isBlank()) {
            CodeTable codeTable = codeTableService.getByTableCode(enumRange);
            if (codeTable == null) {
                throw new BusinessException(StatusCode.VALIDATION_FAILED, "枚举范围引用的码表编码 '" + enumRange + "' 不存在");
            }
            if (codeTable.getStatus() != 1) {
                throw new BusinessException(StatusCode.VALIDATION_FAILED, "枚举范围引用的码表 '" + codeTable.getTableName() + "' 尚未发布，请先发布该码表");
            }
        }
    }

    /**
     * 校验数据类型字段合法性
     *
     * 根据数据类型，校验各字段是否符合互斥规则。
     *
     * @param dataType     数据类型：String / Int / Float / Enum
     * @param length       数据长度
     * @param precision    精度
     * @param rangeMin     取值范围最小值
     * @param rangeMax     取值范围最大值
     * @param enumRange    枚举范围
     * @param checkRequired 是否校验必填字段（新增时需要，编辑时不需要）
     */
    private void validateDataTypeFields(String dataType, Integer length, Integer precision,
                                         String rangeMin, String rangeMax, String enumRange,
                                         boolean checkRequired) {
        switch (dataType) {
            case "String" -> {
                // String 类型不可设置 precision、rangeMin、rangeMax、enumRange
                if (precision != null || hasValue(rangeMin) || hasValue(rangeMax) || hasValue(enumRange)) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "String 类型不可设置精度、取值范围和枚举范围");
                }
                // length 须为正整数
                if (length != null && length <= 0) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "String 类型的数据长度须为正整数");
                }
            }
            case "Int" -> {
                // Int 类型不可设置 length、precision、enumRange
                if (length != null || precision != null || hasValue(enumRange)) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Int 类型不可设置长度、精度和枚举范围");
                }
                // 取值范围只能填整数
                if (hasValue(rangeMin) && !rangeMin.matches("^-?\\d+$")) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Int 类型的取值范围只能填整数");
                }
                if (hasValue(rangeMax) && !rangeMax.matches("^-?\\d+$")) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Int 类型的取值范围只能填整数");
                }
            }
            case "Float" -> {
                // Float 类型不可设置 length、enumRange
                if (length != null || hasValue(enumRange)) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Float 类型不可设置长度和枚举范围");
                }
                // precision 须为非负整数
                if (precision != null && precision < 0) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Float 类型的精度须为非负整数");
                }
                // 取值范围可填整数或实数
                if (hasValue(rangeMin) && !rangeMin.matches("^-?\\d+(\\.\\d+)?$")) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Float 类型的取值范围须为整数或实数");
                }
                if (hasValue(rangeMax) && !rangeMax.matches("^-?\\d+(\\.\\d+)?$")) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Float 类型的取值范围须为整数或实数");
                }
            }
            case "Enum" -> {
                // Enum 类型不可设置 length、precision、rangeMin、rangeMax
                if (length != null || precision != null || hasValue(rangeMin) || hasValue(rangeMax)) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Enum 类型不可设置长度、精度和取值范围");
                }
                // enumRange 必填（仅新增时校验）
                if (checkRequired && (enumRange == null || enumRange.isBlank())) {
                    throw new BusinessException(StatusCode.VALIDATION_FAILED, "Enum 类型的枚举范围不能为空");
                }
            }
        }
    }

    /**
     * 判断字符串是否有值（非 null 且非空）
     */
    private boolean hasValue(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * DataStandard → DataStandardListVo 转换
     */
    private DataStandardListVo convertToListVo(DataStandard entity) {
        DataStandardListVo vo = new DataStandardListVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setEnglishName(entity.getEnglishName());
        vo.setStandardCode(entity.getStandardCode());
        vo.setDataType(entity.getDataType());
        vo.setLength(entity.getLength());
        vo.setPrecision(entity.getPrecision());
        vo.setDefaultValue(entity.getDefaultValue());
        vo.setRangeMin(entity.getRangeMin());
        vo.setRangeMax(entity.getRangeMax());
        vo.setEnumRange(entity.getEnumRange());
        vo.setSourceOrganization(entity.getSourceOrganization());
        vo.setNullable(entity.getNullable());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * DataStandard → DataStandardDetailVo 转换
     */
    private DataStandardDetailVo convertToDetailVo(DataStandard entity) {
        DataStandardDetailVo vo = new DataStandardDetailVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setEnglishName(entity.getEnglishName());
        vo.setStandardCode(entity.getStandardCode());
        vo.setDataType(entity.getDataType());
        vo.setLength(entity.getLength());
        vo.setPrecision(entity.getPrecision());
        vo.setDefaultValue(entity.getDefaultValue());
        vo.setRangeMin(entity.getRangeMin());
        vo.setRangeMax(entity.getRangeMax());
        vo.setEnumRange(entity.getEnumRange());
        vo.setSourceOrganization(entity.getSourceOrganization());
        vo.setNullable(entity.getNullable());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
