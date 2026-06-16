package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.ParamDirection;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.script.ScriptBatchCategoryRequest;
import com.datafactory.common.model.dto.script.ScriptCategoryCreateRequest;
import com.datafactory.common.model.dto.script.ScriptCategoryUpdateRequest;
import com.datafactory.common.model.dto.script.ScriptCreateRequest;
import com.datafactory.common.model.dto.script.ScriptDebugRequest;
import com.datafactory.common.model.dto.script.ScriptParamDTO;
import com.datafactory.common.model.dto.script.ScriptUpdateRequest;
import com.datafactory.common.model.vo.script.ScriptCategoryVo;
import com.datafactory.common.model.vo.script.ScriptDebugVo;
import com.datafactory.common.model.vo.script.ScriptDetailVo;
import com.datafactory.common.model.vo.script.ScriptListVo;
import com.datafactory.common.model.vo.script.ScriptParamVo;
import com.datafactory.core.domain.entity.Script;
import com.datafactory.core.domain.entity.ScriptCategory;
import com.datafactory.core.domain.entity.ScriptParam;
import com.datafactory.core.domain.mapper.ScriptCategoryMapper;
import com.datafactory.core.domain.mapper.ScriptMapper;
import com.datafactory.core.domain.mapper.ScriptParamMapper;
import com.datafactory.core.executor.ScriptExecutorFactory;
import com.datafactory.core.service.IScriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 脚本管理服务实现类
 *
 * 处理脚本分类树的查询与 CRUD，以及脚本的增删改查、在线调试、状态流转和批量操作。
 * 支持 GROOVY（JVM 沙箱执行）和 PYTHON（外部进程执行）两种脚本类型。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IScriptServiceImpl extends ServiceImpl<ScriptMapper, Script> implements IScriptService {

    private final ScriptCategoryMapper scriptCategoryMapper;
    private final ScriptParamMapper scriptParamMapper;
    private final ScriptExecutorFactory scriptExecutorFactory;

    /** 文件上传存储根路径 */
    @Value("${script.file.storage-path:./upload/script}")
    private String scriptStoragePath;

    // ==================== 分类管理 ====================

    @Override
    public List<ScriptCategoryVo> getCategoryTree() {
        // 查询所有分类，按排序号升序
        List<ScriptCategory> allCategories = scriptCategoryMapper.selectList(
                new LambdaQueryWrapper<ScriptCategory>().orderByAsc(ScriptCategory::getSortOrder));
        // 按 parentId 分组
        Map<Long, List<ScriptCategoryVo>> childrenMap = allCategories.stream()
                .map(this::toCategoryVo)
                .collect(Collectors.groupingBy(ScriptCategoryVo::getParentId));
        // 从顶级节点开始构建树
        List<ScriptCategoryVo> tree = new ArrayList<>();
        for (ScriptCategoryVo vo : childrenMap.getOrDefault(0L, Collections.emptyList())) {
            buildCategoryTree(vo, childrenMap);
            tree.add(vo);
        }
        return tree;
    }

    /**
     * 递归构建分类子树
     */
    private void buildCategoryTree(ScriptCategoryVo parent, Map<Long, List<ScriptCategoryVo>> childrenMap) {
        List<ScriptCategoryVo> children = childrenMap.getOrDefault(parent.getId(), Collections.emptyList());
        parent.setChildren(children);
        for (ScriptCategoryVo child : children) {
            buildCategoryTree(child, childrenMap);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScriptCategoryVo createCategory(ScriptCategoryCreateRequest request) {
        // 1. 校验同一父级下名称唯一性
        Long nameCount = scriptCategoryMapper.selectCount(
                new LambdaQueryWrapper<ScriptCategory>()
                        .eq(ScriptCategory::getName, request.getName())
                        .eq(ScriptCategory::getParentId, request.getParentId()));
        if (nameCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "该父级分类下已存在同名分类");
        }

        ScriptCategory category = new ScriptCategory();
        category.setName(request.getName());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        // 2. 计算层级
        if (request.getParentId() != null && request.getParentId() != 0) {
            ScriptCategory parent = scriptCategoryMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException(StatusCode.NOT_FOUND, "父级分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(1);
            category.setParentId(0L);
        }

        scriptCategoryMapper.insert(category);
        log.info("新增脚本分类成功：name={}, id={}", category.getName(), category.getId());
        return toCategoryVo(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, ScriptCategoryUpdateRequest request) {
        ScriptCategory category = scriptCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "分类不存在");
        }
        // 校验同一父级下名称唯一性
        if (!category.getName().equals(request.getName())) {
            Long nameCount = scriptCategoryMapper.selectCount(
                    new LambdaQueryWrapper<ScriptCategory>()
                            .eq(ScriptCategory::getName, request.getName())
                            .eq(ScriptCategory::getParentId, category.getParentId())
                            .ne(ScriptCategory::getId, id));
            if (nameCount > 0) {
                throw new BusinessException(StatusCode.DATA_EXISTS, "该父级分类下已存在同名分类");
            }
        }
        category.setName(request.getName());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        scriptCategoryMapper.updateById(category);
        log.info("编辑脚本分类成功：id={}, name={}", id, request.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        ScriptCategory category = scriptCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "分类不存在");
        }
        // 1. 检查是否有子分类
        Long childCount = scriptCategoryMapper.selectCount(
                new LambdaQueryWrapper<ScriptCategory>().eq(ScriptCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "分类下存在子分类，无法删除");
        }
        // 2. 检查是否有关联脚本
        Long scriptCount = lambdaQuery().eq(Script::getCategoryId, id).count();
        if (scriptCount > 0) {
            throw new BusinessException(StatusCode.RESOURCE_REFERENCED, "分类下关联了脚本，无法删除");
        }
        scriptCategoryMapper.deleteById(id);
        log.info("删除脚本分类成功：id={}", id);
    }

    /**
     * 将分类实体转为 VO
     */
    private ScriptCategoryVo toCategoryVo(ScriptCategory entity) {
        ScriptCategoryVo vo = new ScriptCategoryVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setParentId(entity.getParentId());
        vo.setLevel(entity.getLevel());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreateTime(entity.getCreateTime());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    // ==================== 脚本列表与详情 ====================

    @Override
    public Page<ScriptListVo> getScriptList(Integer pageNum, Integer pageSize, String keyword,
                                            String status, Long categoryId) {
        Page<Script> page = new Page<>(pageNum, pageSize);
        // 排序：状态优先（0→1→2），再按更新时间倒序
        page.addOrder(OrderItem.asc("status"));
        page.addOrder(OrderItem.desc("update_time"));

        LambdaQueryWrapper<Script> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索：模糊匹配脚本名称、说明
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Script::getScriptName, keyword)
                    .or().like(Script::getDescription, keyword));
        }

        // 状态筛选（多值逗号分隔）
        if (status != null && !status.isBlank()) {
            String[] statusArr = status.split(",");
            wrapper.and(w -> {
                for (String s : statusArr) {
                    w.or().eq(Script::getStatus, Integer.parseInt(s.trim()));
                }
            });
        }

        // 分类筛选：查询该分类及其所有后代分类下的脚本
        if (categoryId != null) {
            Set<Long> catIds = getCategoryAndDescendantIds(categoryId);
            if (!catIds.isEmpty()) {
                wrapper.in(Script::getCategoryId, catIds);
            }
        }

        page = baseMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<ScriptListVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ScriptListVo> voList = page.getRecords().stream()
                .map(this::toScriptListVo)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ScriptDetailVo getScriptDetail(Long id) {
        Script script = baseMapper.selectById(id);
        if (script == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "脚本不存在");
        }
        return toScriptDetailVo(script);
    }

    // ==================== 脚本 CRUD ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScriptDetailVo createScript(ScriptCreateRequest request) {
        // 1. 校验 scriptName 唯一性
        Long nameCount = lambdaQuery().eq(Script::getScriptName, request.getScriptName()).count();
        if (nameCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "脚本名称已存在");
        }

        // 2. 校验分类是否存在
        ScriptCategory category = scriptCategoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "所属分类不存在");
        }

        // 3. 校验脚本来源：fileId 和 scriptContent 至少提供一个
        if (request.getFileId() == null && request.getScriptContent() == null) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "脚本文件（fileId）和脚本源代码（scriptContent）至少填一个");
        }
        if (request.getFileId() != null && request.getScriptContent() != null) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "脚本文件和脚本源代码不能同时填写，请选择一种方式");
        }

        // 4. 创建脚本
        Script script = new Script();
        script.setScriptName(request.getScriptName());
        script.setScriptType(request.getScriptType());
        script.setCategoryId(request.getCategoryId());

        // 文件上传模式
        if (request.getFileId() != null) {
            script.setFileId(request.getFileId());
            script.setFileName(getFileNameByFileId(request.getFileId()));
        }

        // 在线编辑模式
        if (request.getScriptContent() != null) {
            script.setScriptContent(request.getScriptContent());
        }

        script.setDescription(request.getDescription());
        script.setStatus(0); // DRAFT
        baseMapper.insert(script);

        // 5. 保存参数定义
        saveScriptParams(script.getId(), request.getInputParams(), ParamDirection.INPUT);
        saveScriptParams(script.getId(), request.getOutputParams(), ParamDirection.OUTPUT);

        log.info("新增脚本成功：name={}, id={}, type={}", script.getScriptName(), script.getId(), script.getScriptType());
        return toScriptDetailVo(script);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScript(Long id, ScriptUpdateRequest request) {
        Script script = baseMapper.selectById(id);
        if (script == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "脚本不存在");
        }
        // 仅未发布和已停用状态可编辑
        if (script.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布状态的脚本不可编辑");
        }

        // 校验 scriptName 唯一性（排除自身）
        Long nameCount = lambdaQuery()
                .eq(Script::getScriptName, request.getScriptName())
                .ne(Script::getId, id)
                .count();
        if (nameCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "脚本名称已存在");
        }

        // 更新基本信息
        script.setScriptName(request.getScriptName());
        if (request.getCategoryId() != null) {
            ScriptCategory category = scriptCategoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException(StatusCode.NOT_FOUND, "所属分类不存在");
            }
            script.setCategoryId(request.getCategoryId());
        }
        if (request.getFileId() != null) {
            script.setFileId(request.getFileId());
            script.setFileName(getFileNameByFileId(request.getFileId()));
            script.setScriptContent(null); // 切换为文件上传模式，清除源代码
        }
        if (request.getScriptContent() != null) {
            script.setScriptContent(request.getScriptContent());
            script.setFileId(null); // 切换为在线编辑模式，清除文件关联
            script.setFileName(null);
        }
        script.setDescription(request.getDescription());
        baseMapper.updateById(script);

        // 更新参数定义：先删后增
        scriptParamMapper.delete(
                new LambdaQueryWrapper<ScriptParam>().eq(ScriptParam::getScriptId, id));
        saveScriptParams(id, request.getInputParams(), ParamDirection.INPUT);
        saveScriptParams(id, request.getOutputParams(), ParamDirection.OUTPUT);

        log.info("编辑脚本成功：id={}, name={}", id, request.getScriptName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScript(Long id) {
        Script script = baseMapper.selectById(id);
        if (script == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "脚本不存在");
        }
        if (script.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅未发布状态的脚本可删除");
        }
        // 删除关联的参数
        scriptParamMapper.delete(
                new LambdaQueryWrapper<ScriptParam>().eq(ScriptParam::getScriptId, id));
        // 删除脚本
        baseMapper.deleteById(id);
        log.info("删除脚本成功：id={}", id);
    }

    // ==================== 状态流转 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishScript(Long id) {
        Script script = baseMapper.selectById(id);
        if (script == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "脚本不存在");
        }
        if (script.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "该脚本已是已发布状态");
        }
        lambdaUpdate().eq(Script::getId, id).set(Script::getStatus, 1).update();
        log.info("发布脚本成功：id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableScript(Long id) {
        Script script = baseMapper.selectById(id);
        if (script == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "脚本不存在");
        }
        if (script.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅已发布状态的脚本可停用");
        }
        lambdaUpdate().eq(Script::getId, id).set(Script::getStatus, 2).update();
        log.info("停用脚本成功：id={}", id);
    }

    // ==================== 调试 ====================

    @Override
    public ScriptDebugVo debugScript(Long id, ScriptDebugRequest request) {
        Script script = baseMapper.selectById(id);
        if (script == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "脚本不存在");
        }

        // 1. 读取脚本源代码：优先使用 scriptContent（在线编辑模式），其次从文件读取（文件上传模式）
        String scriptContent = script.getScriptContent();
        if (scriptContent == null || scriptContent.isBlank()) {
            scriptContent = readScriptFileContent(script);
        }
        if (scriptContent == null || scriptContent.isBlank()) {
            throw new BusinessException(StatusCode.BUSINESS_ERROR, "脚本源代码为空或文件不存在，无法调试");
        }

        // 2. 根据脚本类型获取执行器并执行
        Map<String, Object> params = request.getParams() != null ? request.getParams() : new HashMap<>();
        ScriptDebugVo result = scriptExecutorFactory.getExecutor(script.getScriptType())
                .execute(scriptContent, params);

        log.info("脚本调试完成：id={}, type={}, success={}, time={}ms",
                id, script.getScriptType(), result.getSuccess(), result.getExecuteTime());
        return result;
    }

    // ==================== 批量操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选脚本
        List<Script> scriptList = lambdaQuery().in(Script::getId, ids).list();

        // 2. 校验数据完整性
        if (scriptList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分脚本不存在");
        }

        // 3. 校验是否包含已发布脚本（未发布和已停用均可发布）
        boolean hasInvalid = scriptList.stream().anyMatch(a -> a.getStatus() == 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED,
                    "所选脚本中包含已发布状态的数据，操作不合法");
        }

        // 4. 批量更新为已发布
        lambdaUpdate().in(Script::getId, ids).set(Script::getStatus, 1).update();
        log.info("批量发布脚本成功：ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisable(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选脚本
        List<Script> scriptList = lambdaQuery().in(Script::getId, ids).list();

        // 2. 校验数据完整性
        if (scriptList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分脚本不存在");
        }

        // 3. 校验是否均为已发布状态
        boolean hasInvalid = scriptList.stream().anyMatch(a -> a.getStatus() != 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED,
                    "所选脚本中包含未发布或已停用状态的数据，操作不合法");
        }

        // 4. 批量更新为已停用
        lambdaUpdate().in(Script::getId, ids).set(Script::getStatus, 2).update();
        log.info("批量停用脚本成功：ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCategory(ScriptBatchCategoryRequest request) {
        List<Long> ids = request.getIds();
        Long categoryId = request.getCategoryId();

        // 1. 校验分类是否存在
        ScriptCategory category = scriptCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "目标分类不存在");
        }

        // 2. 查询所选脚本
        List<Script> scriptList = lambdaQuery().in(Script::getId, ids).list();

        // 3. 校验数据完整性
        if (scriptList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分脚本不存在");
        }

        // 4. 校验是否包含已发布状态（已发布脚本不可移动分类）
        boolean hasPublished = scriptList.stream().anyMatch(a -> a.getStatus() == 1);
        if (hasPublished) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED,
                    "所选脚本中包含已发布状态的数据，操作不合法");
        }

        // 5. 批量更新分类
        lambdaUpdate().in(Script::getId, ids).set(Script::getCategoryId, categoryId).update();
        log.info("批量修改脚本分类成功：ids={}, categoryId={}", ids, categoryId);
    }

    // ==================== 私有工具方法 ====================

    /**
     * 获取指定分类及其所有后代分类的ID集合
     *
     * @param categoryId 起始分类ID
     * @return 包含自身及所有后代的ID集合
     */
    private Set<Long> getCategoryAndDescendantIds(Long categoryId) {
        Set<Long> result = new HashSet<>();
        result.add(categoryId);
        // 查询所有分类
        List<ScriptCategory> allCategories = scriptCategoryMapper.selectList(null);
        Map<Long, List<ScriptCategory>> parentMap = allCategories.stream()
                .collect(Collectors.groupingBy(ScriptCategory::getParentId));
        // BFS 收集后代
        Queue<Long> queue = new LinkedList<>();
        queue.add(categoryId);
        while (!queue.isEmpty()) {
            Long parentId = queue.poll();
            List<ScriptCategory> children = parentMap.getOrDefault(parentId, Collections.emptyList());
            for (ScriptCategory child : children) {
                Long childId = child.getId();
                if (result.add(childId)) {
                    queue.add(childId);
                }
            }
        }
        return result;
    }

    /**
     * 根据分类ID查询分类名称
     *
     * @param categoryId 分类ID
     * @return 分类名称（分类不存在时返回 null）
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        ScriptCategory category = scriptCategoryMapper.selectById(categoryId);
        return category != null ? category.getName() : null;
    }

    /**
     * 将 Script 转为列表 VO
     */
    private ScriptListVo toScriptListVo(Script script) {
        ScriptListVo vo = new ScriptListVo();
        vo.setId(script.getId());
        vo.setScriptName(script.getScriptName());
        vo.setScriptType(script.getScriptType());
        vo.setCategoryId(script.getCategoryId());
        vo.setCategoryName(getCategoryName(script.getCategoryId()));
        vo.setFileName(script.getFileName());
        vo.setDescription(script.getDescription());
        vo.setStatus(script.getStatus());
        vo.setCreateTime(script.getCreateTime());
        vo.setUpdateTime(script.getUpdateTime());
        return vo;
    }

    /**
     * 将 Script 转为详情 VO
     */
    private ScriptDetailVo toScriptDetailVo(Script script) {
        ScriptDetailVo vo = new ScriptDetailVo();
        vo.setId(script.getId());
        vo.setScriptName(script.getScriptName());
        vo.setScriptType(script.getScriptType());
        vo.setCategoryId(script.getCategoryId());
        vo.setCategoryName(getCategoryName(script.getCategoryId()));
        vo.setFileId(script.getFileId());
        vo.setFileName(script.getFileName());
        vo.setScriptContent(script.getScriptContent());
        vo.setDescription(script.getDescription());
        vo.setStatus(script.getStatus());
        vo.setCreateTime(script.getCreateTime());
        vo.setUpdateTime(script.getUpdateTime());

        // 查询输入参数
        List<ScriptParam> inputParams = scriptParamMapper.selectList(
                new LambdaQueryWrapper<ScriptParam>()
                        .eq(ScriptParam::getScriptId, script.getId())
                        .eq(ScriptParam::getParamDirection, ParamDirection.INPUT));
        vo.setInputParams(inputParams.stream().map(this::toParamVo).collect(Collectors.toList()));

        // 查询输出参数
        List<ScriptParam> outputParams = scriptParamMapper.selectList(
                new LambdaQueryWrapper<ScriptParam>()
                        .eq(ScriptParam::getScriptId, script.getId())
                        .eq(ScriptParam::getParamDirection, ParamDirection.OUTPUT));
        vo.setOutputParams(outputParams.stream().map(this::toParamVo).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 将 ScriptParam 转为参数 VO
     */
    private ScriptParamVo toParamVo(ScriptParam param) {
        ScriptParamVo vo = new ScriptParamVo();
        vo.setId(param.getId());
        vo.setParamName(param.getParamName());
        vo.setParamType(param.getParamType());
        vo.setDescription(param.getDescription());
        return vo;
    }

    /**
     * 保存脚本参数定义
     *
     * @param scriptId  脚本ID
     * @param paramDTOs 参数定义列表
     * @param direction 参数方向
     */
    private void saveScriptParams(Long scriptId, List<ScriptParamDTO> paramDTOs, ParamDirection direction) {
        if (paramDTOs == null || paramDTOs.isEmpty()) {
            return;
        }
        for (ScriptParamDTO dto : paramDTOs) {
            ScriptParam param = new ScriptParam();
            param.setScriptId(scriptId);
            param.setParamName(dto.getParamName());
            param.setParamType(dto.getParamType());
            param.setParamDirection(direction);
            param.setDescription(dto.getDescription());
            scriptParamMapper.insert(param);
        }
    }

    /**
     * 根据 fileId 获取文件名
     *
     * 目前返回占位值，实际应用中需通过文件管理服务获取。
     *
     * @param fileId 文件ID
     * @return 文件名
     */
    private String getFileNameByFileId(Long fileId) {
        // 从文件存储路径读取文件名信息
        Path filePath = Paths.get(scriptStoragePath, String.valueOf(fileId));
        if (Files.exists(filePath)) {
            return filePath.getFileName().toString();
        }
        return String.valueOf(fileId);
    }

    /**
     * 读取脚本文件内容
     *
     * @param script 脚本实体
     * @return 文件内容，读取失败返回 null
     */
    private String readScriptFileContent(Script script) {
        try {
            Path filePath = Paths.get(scriptStoragePath, String.valueOf(script.getFileId()));
            if (Files.exists(filePath)) {
                return Files.readString(filePath, StandardCharsets.UTF_8);
            }
            log.warn("脚本文件不存在：fileId={}, path={}", script.getFileId(), filePath);
            return null;
        } catch (IOException e) {
            log.error("读取脚本文件失败：fileId={}", script.getFileId(), e);
            return null;
        }
    }
}
