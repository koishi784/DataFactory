package com.datafactory.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.datastandard.DataStandardCreateRequest;
import com.datafactory.common.model.dto.datastandard.DataStandardUpdateRequest;
import com.datafactory.common.model.vo.datastandard.DataStandardDetailVo;
import com.datafactory.common.model.vo.datastandard.DataStandardImportResultVo;
import com.datafactory.common.model.vo.datastandard.DataStandardListVo;
import com.datafactory.common.utils.StatusUtils;
import com.datafactory.core.domain.entity.CodeTable;
import com.datafactory.core.domain.entity.DataStandard;
import com.datafactory.core.domain.mapper.DataStandardMapper;
import com.datafactory.core.model.DataStandardImportRow;
import com.datafactory.core.service.ICodeTableService;
import com.datafactory.core.service.IDataStandardService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据标准服务实现类
 *
 * 实现数据标准的 CRUD、状态管理、批量操作、模板下载、Excel 导入及数据类型校验等完整业务逻辑。
 * 标准编号由系统自动生成（格式 BZ + 5 位数字），基于保存后的 ID 生成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IDataStandardServiceImpl extends ServiceImpl<DataStandardMapper, DataStandard> implements IDataStandardService {

    private final ICodeTableService codeTableService;

    /** 导入记录条数上限 */
    private static final int IMPORT_MAX_RECORDS = 10000;

    /** 模板表头列 */
    private static final List<List<String>> TEMPLATE_HEADERS = List.of(
            List.of("中文名称", "英文名称", "数据类型", "数据长度", "数据精度", "默认值",
                    "取值范围最小值", "取值范围最大值", "引用码表编号", "来源机构",
                    "是否可为空", "标准说明")
    );

    /**
     * 导入标准 Excel 读取监听器
     *
     * 逐行读取 Excel 数据并存储到行列表中，同时记录每个数据的行号。
     */
    private static class ImportReadListener implements ReadListener<DataStandardImportRow> {

        private final List<DataStandardImportRow> rows = new ArrayList<>();
        private int currentRow = 1; // 表头为第 1 行，数据从第 2 行开始

        @Override
        public void invoke(DataStandardImportRow row, AnalysisContext context) {
            currentRow++;
            row.setRowIndex(currentRow);
            rows.add(row);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            log.info("Excel 导入解析完成，共读取 {} 条数据", rows.size());
        }

        public List<DataStandardImportRow> getRows() {
            return rows;
        }
    }

    // ==================== 列表/详情/CRUD/状态管理 ====================

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

        // 2. 枚举范围校验（通过码表服务校验）
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

        // 4. 枚举范围校验（通过码表服务校验）
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

    // ==================== 模板下载 ====================

    /**
     * 下载导入模板
     *
     * 使用 EasyExcel 生成包含表头行的 Excel 文件（.xlsx），无示例数据行。
     * 设置响应头为文件下载格式。
     *
     * @param response HTTP 响应
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 1. 设置响应头（文件下载）
        String fileName = URLEncoder.encode("数据标准导入模板", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition",
                "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 2. 使用 EasyExcel 写入模板
        try (var outputStream = response.getOutputStream()) {
            EasyExcel.write(outputStream)
                    .head(TEMPLATE_HEADERS)
                    .sheet("数据标准导入模板")
                    .doWrite(List.of()); // 无数据行，仅表头
        }
        log.info("数据标准导入模板下载成功");
    }

    // ==================== 标准导入 ====================

    /**
     * 批量导入数据标准
     *
     * 通过上传的 Excel 文件批量导入数据标准，按六步规则进行校验和过滤：
     * 1. 必填项校验 → 2. 文件内去重 → 3. 字段合法性校验 → 4. 数据类型互斥校验
     * → 5. 系统内去重 → 6. 引用码表校验
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataStandardImportResultVo importStandards(MultipartFile file) throws IOException {
        DataStandardImportResultVo result = new DataStandardImportResultVo();

        // 0. 读取 Excel 文件
        ImportReadListener listener = new ImportReadListener();
        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream, DataStandardImportRow.class, listener)
                    .sheet()
                    .doRead();
        }
        List<DataStandardImportRow> rows = listener.getRows();

        // 校验总条数限制（最多 10000 条）
        if (rows.size() > IMPORT_MAX_RECORDS) {
            throw new BusinessException(StatusCode.VALIDATION_FAILED,
                    "导入数据条数不能超过 " + IMPORT_MAX_RECORDS + " 条，当前文件共 " + rows.size() + " 条");
        }

        result.setTotalCount(rows.size());

        // 步骤一：必填项校验（过滤空行）
        List<DataStandardImportRow> step1Result = step1ValidateRequiredFields(rows, result);

        // 步骤二：文件内去重
        List<DataStandardImportRow> step2Result = step2DeduplicateWithinFile(step1Result, result);

        // 步骤三：字段合法性校验
        List<DataStandardImportRow> step3Result = step3ValidateFieldFormat(step2Result, result);

        // 步骤四：数据类型字段互斥校验
        List<DataStandardImportRow> step4Result = step4ValidateDataTypeMutualExclusion(step3Result, result);

        // 步骤五：与系统中已有的标准去重
        List<DataStandardImportRow> step5Result = step5DeduplicateWithSystem(step4Result, result);

        // 步骤六：引用码表校验
        List<DataStandardImportRow> step6Result = step6ValidateEnumRange(step5Result, result);

        // 批量写入数据库
        if (!step6Result.isEmpty()) {
            batchInsertImportRows(step6Result);
            result.setSuccessCount(step6Result.size());
        }

        result.setFailCount(result.getTotalCount() - result.getSuccessCount());
        log.info("数据标准导入完成：总={}，成功={}，失败={}", result.getTotalCount(), result.getSuccessCount(), result.getFailCount());
        return result;
    }

    // ==================== 导入各校验步骤 ====================

    /**
     * 步骤一：必填项校验
     *
     * 过滤掉中文名称、英文名称、来源机构、数据类型中任意字段为空或全空格的数据行。
     */
    private List<DataStandardImportRow> step1ValidateRequiredFields(
            List<DataStandardImportRow> rows, DataStandardImportResultVo result) {
        List<DataStandardImportRow> passed = new ArrayList<>();
        for (DataStandardImportRow row : rows) {
            List<String> missing = new ArrayList<>();
            if (isBlank(row.getName())) missing.add("中文名称");
            if (isBlank(row.getEnglishName())) missing.add("英文名称");
            if (isBlank(row.getSourceOrganization())) missing.add("来源机构");
            if (isBlank(row.getDataType())) missing.add("数据类型");

            if (!missing.isEmpty()) {
                result.getFailDetails().add(new DataStandardImportResultVo.FailDetail(
                        row.getRowIndex(), missing + "为空"));
            } else {
                // 去除空格
                row.setName(row.getName().trim());
                row.setEnglishName(row.getEnglishName().trim());
                row.setSourceOrganization(row.getSourceOrganization().trim());
                row.setDataType(row.getDataType().trim());
                passed.add(row);
            }
        }
        return passed;
    }

    /**
     * 步骤二：文件内去重
     *
     * 若中文名称或英文名称重复，只保留重复项中最上方的一条。
     */
    private List<DataStandardImportRow> step2DeduplicateWithinFile(
            List<DataStandardImportRow> rows, DataStandardImportResultVo result) {
        List<DataStandardImportRow> passed = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        Set<String> seenEnglishNames = new HashSet<>();

        for (DataStandardImportRow row : rows) {
            String nameKey = row.getName().toLowerCase();
            String engKey = row.getEnglishName().toLowerCase();
            boolean nameDup = seenNames.contains(nameKey);
            boolean engDup = seenEnglishNames.contains(engKey);

            if (nameDup) {
                result.getFailDetails().add(new DataStandardImportResultVo.FailDetail(
                        row.getRowIndex(), "中文名称 '" + row.getName() + "' 在文件中重复"));
            }
            if (engDup) {
                result.getFailDetails().add(new DataStandardImportResultVo.FailDetail(
                        row.getRowIndex(), "英文名称 '" + row.getEnglishName() + "' 在文件中重复"));
            }

            if (!nameDup && !engDup) {
                seenNames.add(nameKey);
                seenEnglishNames.add(engKey);
                passed.add(row);
            }
        }
        return passed;
    }

    /**
     * 步骤三：字段合法性校验
     *
     * 校验各字段的格式合法性：中文名称（仅中英文）、英文名称（字母数字下划线，英文开头）、
     * 数据类型（仅四种）、数据长度（正整数）、数据精度（非负整数）、取值范围格式等。
     */
    private List<DataStandardImportRow> step3ValidateFieldFormat(
            List<DataStandardImportRow> rows, DataStandardImportResultVo result) {
        List<DataStandardImportRow> passed = new ArrayList<>();

        for (DataStandardImportRow row : rows) {
            List<String> errors = new ArrayList<>();

            // 中文名称：仅支持中文及英文大小写
            if (!row.getName().matches("^[\\u4e00-\\u9fa5a-zA-Z]+$")) {
                errors.add("中文名称仅支持中文及英文大小写");
            }

            // 英文名称：仅支持英文大小写、数字及下划线，英文开头
            if (!row.getEnglishName().matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                errors.add("英文名称仅支持英文大小写、数字及下划线，且以英文字母开头");
            }

            // 数据类型：仅支持四种
            if (!List.of("String", "Int", "Float", "Enum").contains(row.getDataType())) {
                errors.add("数据类型仅支持 String / Int / Float / Enum");
            }

            // 数据长度（有值时）：须为正整数
            if (row.getLength() != null && row.getLength() <= 0) {
                errors.add("数据长度须为正整数");
            }

            // 数据精度（有值时）：须为非负整数
            if (row.getPrecision() != null && row.getPrecision() < 0) {
                errors.add("数据精度须为非负整数");
            }

            // 取值范围（Int 类型）：只能填整数
            if ("Int".equals(row.getDataType())) {
                if (hasValue(row.getRangeMin()) && !row.getRangeMin().matches("^-?\\d+$")) {
                    errors.add("Int 类型的取值范围最小值只能填整数");
                }
                if (hasValue(row.getRangeMax()) && !row.getRangeMax().matches("^-?\\d+$")) {
                    errors.add("Int 类型的取值范围最大值只能填整数");
                }
            }

            // 取值范围（Float 类型）：可填整数或实数
            if ("Float".equals(row.getDataType())) {
                if (hasValue(row.getRangeMin()) && !row.getRangeMin().matches("^-?\\d+(\\.\\d+)?$")) {
                    errors.add("Float 类型的取值范围最小值须为整数或实数");
                }
                if (hasValue(row.getRangeMax()) && !row.getRangeMax().matches("^-?\\d+(\\.\\d+)?$")) {
                    errors.add("Float 类型的取值范围最大值须为整数或实数");
                }
            }

            if (!errors.isEmpty()) {
                result.getFailDetails().add(new DataStandardImportResultVo.FailDetail(
                        row.getRowIndex(), String.join("；", errors)));
            } else {
                passed.add(row);
            }
        }
        return passed;
    }

    /**
     * 步骤四：数据类型字段互斥校验
     *
     * 根据数据类型（String / Int / Float / Enum）校验字段互斥规则。
     */
    private List<DataStandardImportRow> step4ValidateDataTypeMutualExclusion(
            List<DataStandardImportRow> rows, DataStandardImportResultVo result) {
        List<DataStandardImportRow> passed = new ArrayList<>();

        for (DataStandardImportRow row : rows) {
            List<String> errors = new ArrayList<>();

            switch (row.getDataType()) {
                case "String" -> {
                    if (row.getPrecision() != null || hasValue(row.getRangeMin())
                            || hasValue(row.getRangeMax()) || hasValue(row.getEnumRange())) {
                        errors.add("String 类型不可设置精度、取值范围和枚举范围");
                    }
                }
                case "Int" -> {
                    if (row.getLength() != null || row.getPrecision() != null || hasValue(row.getEnumRange())) {
                        errors.add("Int 类型不可设置长度、精度和枚举范围");
                    }
                }
                case "Float" -> {
                    if (row.getLength() != null || hasValue(row.getEnumRange())) {
                        errors.add("Float 类型不可设置长度和枚举范围");
                    }
                }
                case "Enum" -> {
                    if (row.getLength() != null || row.getPrecision() != null
                            || hasValue(row.getRangeMin()) || hasValue(row.getRangeMax())) {
                        errors.add("Enum 类型不可设置长度、精度和取值范围");
                    }
                }
            }

            if (!errors.isEmpty()) {
                result.getFailDetails().add(new DataStandardImportResultVo.FailDetail(
                        row.getRowIndex(), String.join("；", errors)));
            } else {
                passed.add(row);
            }
        }
        return passed;
    }

    /**
     * 步骤五：与系统中已有的标准去重
     *
     * 若中文名称或英文名称与系统中的标准重复，则过滤掉该条标准。
     */
    private List<DataStandardImportRow> step5DeduplicateWithSystem(
            List<DataStandardImportRow> rows, DataStandardImportResultVo result) {
        if (rows.isEmpty()) {
            return rows;
        }

        // 收集需要查询的英文名称
        List<String> engNames = rows.stream()
                .map(DataStandardImportRow::getEnglishName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        List<String> chnNames = rows.stream()
                .map(DataStandardImportRow::getName)
                .collect(Collectors.toList());

        // 批量查询系统中已存在的标准（不区分大小写）
        List<DataStandard> existing = lambdaQuery()
                .and(w -> {
                    w.in(DataStandard::getEnglishName, engNames);
                    for (String name : chnNames) {
                        w.or().eq(DataStandard::getName, name);
                    }
                })
                .list();

        Set<String> existingEngNames = existing.stream()
                .map(DataStandard::getEnglishName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        Set<String> existingChnNames = existing.stream()
                .map(DataStandard::getName)
                .collect(Collectors.toSet());

        List<DataStandardImportRow> passed = new ArrayList<>();
        for (DataStandardImportRow row : rows) {
            if (existingChnNames.contains(row.getName())) {
                result.getFailDetails().add(new DataStandardImportResultVo.FailDetail(
                        row.getRowIndex(), "中文名称 '" + row.getName() + "' 与系统中已存在的标准重复"));
            } else if (existingEngNames.contains(row.getEnglishName().toLowerCase())) {
                result.getFailDetails().add(new DataStandardImportResultVo.FailDetail(
                        row.getRowIndex(), "英文名称 '" + row.getEnglishName() + "' 与系统中已存在的标准重复"));
            } else {
                passed.add(row);
            }
        }
        return passed;
    }

    /**
     * 步骤六：引用码表校验
     *
     * 对于 Enum 类型，校验引用码表编号对应的码表存在且已发布。
     */
    private List<DataStandardImportRow> step6ValidateEnumRange(
            List<DataStandardImportRow> rows, DataStandardImportResultVo result) {
        List<DataStandardImportRow> passed = new ArrayList<>();

        for (DataStandardImportRow row : rows) {
            if (!"Enum".equals(row.getDataType()) || !hasValue(row.getEnumRange())) {
                passed.add(row);
                continue;
            }

            try {
                validateEnumRange(row.getEnumRange());
                passed.add(row);
            } catch (BusinessException e) {
                result.getFailDetails().add(new DataStandardImportResultVo.FailDetail(
                        row.getRowIndex(), e.getMessage()));
            }
        }
        return passed;
    }

    // ==================== 批量插入 ====================

    /**
     * 批量插入导入的数据标准
     *
     * 逐条保存（每条需生成标准编号），开启事务保证原子性。
     *
     * @param rows 经校验通过的导入行列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertImportRows(List<DataStandardImportRow> rows) {
        for (DataStandardImportRow row : rows) {
            DataStandard standard = new DataStandard();
            standard.setName(row.getName());
            standard.setEnglishName(row.getEnglishName());
            standard.setDataType(row.getDataType());
            standard.setLength(row.getLength());
            standard.setPrecision(row.getPrecision());
            standard.setDefaultValue(row.getDefaultValue());
            standard.setRangeMin(row.getRangeMin());
            standard.setRangeMax(row.getRangeMax());
            standard.setEnumRange(row.getEnumRange());
            standard.setSourceOrganization(row.getSourceOrganization());
            standard.setNullable(row.getNullable() != null ? row.getNullable() : 0);
            standard.setDescription(row.getDescription());
            standard.setStandardCode(""); // 占位，保存后立即更新
            standard.setStatus(0); // DRAFT

            save(standard);

            // 生成标准编号
            standard.setStandardCode(generateStandardCode(standard.getId()));
            updateById(standard);
        }
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
     * @param dataType      数据类型：String / Int / Float / Enum
     * @param length        数据长度
     * @param precision     精度
     * @param rangeMin      取值范围最小值
     * @param rangeMax      取值范围最大值
     * @param enumRange     枚举范围
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
     * 判断字符串是否为空或全空格
     */
    private boolean isBlank(String str) {
        return str == null || str.isBlank();
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
