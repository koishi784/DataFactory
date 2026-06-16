package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.task.*;
import com.datafactory.common.model.vo.task.*;
import com.datafactory.core.domain.entity.Task;
import com.datafactory.core.domain.entity.TaskEdge;
import com.datafactory.core.domain.entity.TaskExecution;
import com.datafactory.core.domain.entity.TaskNode;
import com.datafactory.core.domain.entity.TaskNodeExecution;
import com.datafactory.core.domain.mapper.*;
import com.datafactory.core.service.ITaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务管理服务实现类
 *
 * 实现任务的 CRUD、DAG 编排、触发配置、执行管理、状态变更等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ITaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements ITaskService {

    private final TaskNodeMapper taskNodeMapper;
    private final TaskEdgeMapper taskEdgeMapper;
    private final TaskExecutionMapper taskExecutionMapper;
    private final TaskNodeExecutionMapper taskNodeExecutionMapper;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 查询 ====================

    /**
     * 分页查询任务列表
     *
     * 排序规则：优先级一按状态（DRAFT→PUBLISHED→DISABLED），优先级二按更新时间倒序
     */
    @Override
    public IPage<TaskListVo> getTaskPage(Integer pageNum, Integer pageSize,
                                         String keyword, String status,
                                         Long categoryId, Integer executeStatus) {
        Page<Task> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        // 关键词模糊匹配
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(Task::getTaskName, keyword)
                    .or()
                    .like(Task::getTaskDescription, keyword));
        }

        // 状态筛选
        if (StringUtils.isNotBlank(status)) {
            List<Integer> statusList = Arrays.stream(status.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            wrapper.in(Task::getStatus, statusList);
        }

        // 分类筛选（含后代分类，此处简化处理为精确匹配）
        if (categoryId != null) {
            wrapper.eq(Task::getCategoryId, categoryId);
        }

        // 执行状态筛选
        if (executeStatus != null) {
            wrapper.eq(Task::getExecuteStatus, executeStatus);
        }

        // 排序：先按状态升序（0→1→2），再按更新时间倒序
        wrapper.orderByAsc(Task::getStatus)
                .orderByDesc(Task::getUpdateTime);

        Page<Task> taskPage = baseMapper.selectPage(page, wrapper);

        // 转换为 VO
        return taskPage.convert(this::convertToListVo);
    }

    /**
     * 查询任务详情
     */
    @Override
    public TaskDetailVo getTaskDetail(Long id) {
        // 1. 查询任务基本信息
        Task task = lambdaQuery()
                .eq(Task::getId, id)
                .one();
        if (task == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "任务不存在");
        }

        // 2. 转换为详情 VO
        TaskDetailVo detailVo = convertToDetailVo(task);

        // 3. 查询 DAG 节点
        List<TaskNode> nodes = taskNodeMapper.selectList(
                new LambdaQueryWrapper<TaskNode>()
                        .eq(TaskNode::getTaskId, id)
                        .orderByAsc(TaskNode::getCreateTime));
        detailVo.setNodes(nodes.stream().map(this::convertToNodeDetail).collect(Collectors.toList()));

        // 4. 查询 DAG 连线
        List<TaskEdge> edges = taskEdgeMapper.selectList(
                new LambdaQueryWrapper<TaskEdge>()
                        .eq(TaskEdge::getTaskId, id));
        List<TaskEdgeDto> edgeDtos = edges.stream().map(edge -> {
            TaskEdgeDto dto = new TaskEdgeDto();
            dto.setEdgeId(edge.getEdgeId());
            dto.setSourceNodeId(edge.getSourceNodeId());
            dto.setTargetNodeId(edge.getTargetNodeId());
            dto.setCondition(edge.getCondition());
            return dto;
        }).collect(Collectors.toList());
        detailVo.setEdges(edgeDtos);

        // 5. 查询触发配置
        TaskDetailVo.TriggerConfigVo triggerConfig = buildTriggerConfig(task);
        detailVo.setTriggerConfig(triggerConfig);

        return detailVo;
    }

    /**
     * 分页查询任务执行历史
     */
    @Override
    public IPage<TaskExecutionVo> getExecutionHistory(Long taskId, Integer pageNum, Integer pageSize,
                                                       String startDate, String endDate, String status) {
        Page<TaskExecution> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<TaskExecution> wrapper = new LambdaQueryWrapper<TaskExecution>()
                .eq(TaskExecution::getTaskId, taskId);

        // 日期范围筛选
        if (StringUtils.isNotBlank(startDate)) {
            wrapper.ge(TaskExecution::getStartTime, startDate + " 00:00:00");
        }
        if (StringUtils.isNotBlank(endDate)) {
            wrapper.le(TaskExecution::getStartTime, endDate + " 23:59:59");
        }

        // 执行状态筛选
        if (StringUtils.isNotBlank(status)) {
            List<Integer> statusList = Arrays.stream(status.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            wrapper.in(TaskExecution::getStatus, statusList);
        }

        // 按开始时间倒序
        wrapper.orderByDesc(TaskExecution::getStartTime);

        Page<TaskExecution> executionPage = taskExecutionMapper.selectPage(page, wrapper);

        return executionPage.convert(this::convertToExecutionVo);
    }

    // ==================== 新增/编辑 ====================

    /**
     * 新增任务（基本信息）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task createTask(TaskCreateRequest request) {
        // 1. 校验任务名称唯一性
        Long existsCount = lambdaQuery()
                .eq(Task::getTaskName, request.getTaskName())
                .count();
        if (existsCount > 0) {
            throw new BusinessException(StatusCode.DATA_EXISTS, "任务名称已存在");
        }

        // 2. 构建实体
        Task task = new Task();
        task.setTaskName(request.getTaskName());
        task.setTaskDescription(request.getTaskDescription());
        task.setCategoryId(request.getCategoryId());
        task.setStatus(0); // 未发布
        task.setExecuteStatus(0); // 等待

        // 3. 保存
        save(task);

        log.info("新增任务成功：id={}, taskName={}", task.getId(), request.getTaskName());
        return task;
    }

    /**
     * 更新任务 DAG 配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskConfig(Long id, TaskConfigRequest request) {
        // 1. 校验任务存在且状态允许
        Task task = lambdaQuery()
                .eq(Task::getId, id)
                .one();
        if (task == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "任务不存在");
        }
        if (task.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布状态的任务不可编辑 DAG 配置");
        }

        // 2. 校验至少包含 START 和 END 节点
        List<TaskNodeDto> nodes = request.getNodes();
        List<TaskEdgeDto> edges = request.getEdges();

        boolean hasStart = nodes.stream().anyMatch(n -> "START".equals(n.getNodeType()));
        boolean hasEnd = nodes.stream().anyMatch(n -> "END".equals(n.getNodeType()));
        if (!hasStart || !hasEnd) {
            throw new BusinessException(StatusCode.VALIDATION_FAILED, "DAG 必须包含 START 和 END 节点");
        }

        // 3. 先删除旧的节点和连线
        taskNodeMapper.delete(new LambdaQueryWrapper<TaskNode>().eq(TaskNode::getTaskId, id));
        taskEdgeMapper.delete(new LambdaQueryWrapper<TaskEdge>().eq(TaskEdge::getTaskId, id));

        // 4. 保存新的节点
        for (TaskNodeDto nodeDto : nodes) {
            TaskNode taskNode = new TaskNode();
            taskNode.setTaskId(id);
            taskNode.setNodeId(nodeDto.getNodeId());
            taskNode.setNodeName(nodeDto.getNodeName());
            taskNode.setNodeType(nodeDto.getNodeType());
            taskNode.setPositionX(nodeDto.getPositionX());
            taskNode.setPositionY(nodeDto.getPositionY());
            taskNode.setNodeConfig(nodeDto.getConfig());
            taskNode.setCreateTime(LocalDateTime.now());
            taskNode.setUpdateTime(LocalDateTime.now());
            taskNodeMapper.insert(taskNode);
        }

        // 5. 保存新的连线
        if (edges != null) {
            for (TaskEdgeDto edgeDto : edges) {
                TaskEdge taskEdge = new TaskEdge();
                taskEdge.setTaskId(id);
                taskEdge.setEdgeId(edgeDto.getEdgeId());
                taskEdge.setSourceNodeId(edgeDto.getSourceNodeId());
                taskEdge.setTargetNodeId(edgeDto.getTargetNodeId());
                taskEdge.setCondition(edgeDto.getCondition());
                taskEdgeMapper.insert(taskEdge);
            }
        }

        log.info("更新任务 DAG 配置成功：taskId={}, nodeCount={}, edgeCount={}", id, nodes.size(), edges != null ? edges.size() : 0);
    }

    /**
     * 任务触发设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTriggerConfig(Long id, TaskTriggerConfigRequest request) {
        // 1. 校验任务存在
        Task task = lambdaQuery()
                .eq(Task::getId, id)
                .one();
        if (task == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "任务不存在");
        }

        // 2. 更新调度类型
        task.setScheduleType(request.getScheduleType());

        if ("API".equals(request.getScheduleType())) {
            // API 模式：保存 API 配置信息到任务扩展字段
            // 注意：API 注册到接口管理功能涉及跨模块操作，此处仅保存基本信息
            if (request.getApiConfig() != null) {
                // API 配置信息通过任务的备注或其他扩展字段保存
                // 完整的 API 注册逻辑由 Controller 层或后续步骤完成
                log.info("任务触发设置 - API 模式：taskId={}, apiName={}", id, request.getApiConfig().getApiName());
            }
            task.setCronExpression(null);
            task.setEffectiveDate(null);
            task.setExpireDate(null);
        } else if ("CRON".equals(request.getScheduleType())) {
            // CRON 模式
            task.setEffectiveDate(request.getEffectiveDate());
            task.setExpireDate(request.getExpireDate());

            // 如果有定时调度配置，转换为 Cron 表达式保存
            if (request.getScheduleConfig() != null) {
                String cronExpr = buildCronExpression(request.getScheduleConfig());
                task.setCronExpression(cronExpr);
            }

            log.info("任务触发设置 - CRON 模式：taskId={}, cron={}", id, task.getCronExpression());
        } else {
            throw new BusinessException(StatusCode.BAD_REQUEST, "不支持的调度类型，仅支持 API 和 CRON");
        }

        // 3. 保存
        lambdaUpdate()
                .eq(Task::getId, id)
                .update(task);

        log.info("更新任务触发设置成功：taskId={}, scheduleType={}", id, request.getScheduleType());
    }

    // ==================== 状态变更 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Task task = getTaskOrThrow(id);
        if (task.getStatus() == 0 || task.getStatus() == 2) {
            lambdaUpdate()
                    .eq(Task::getId, id)
                    .set(Task::getStatus, 1)
                    .update();
            log.info("发布任务成功：id={}, taskName={}", id, task.getTaskName());
        } else if (task.getStatus() == 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "任务已发布，无需重复操作");
        } else {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "任务状态不允许发布");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        Task task = getTaskOrThrow(id);
        if (task.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅已发布状态的任务可停用");
        }
        lambdaUpdate()
                .eq(Task::getId, id)
                .set(Task::getStatus, 2)
                .update();
        log.info("停用任务成功：id={}, taskName={}", id, task.getTaskName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        Task task = getTaskOrThrow(id);
        if (task.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅未发布状态的任务可删除");
        }
        // 物理删除 DAG 节点和连线
        taskNodeMapper.delete(new LambdaQueryWrapper<TaskNode>().eq(TaskNode::getTaskId, id));
        taskEdgeMapper.delete(new LambdaQueryWrapper<TaskEdge>().eq(TaskEdge::getTaskId, id));
        // 逻辑删除任务本身
        removeById(id);
        log.info("删除任务成功：id={}, taskName={}", id, task.getTaskName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "ID 列表不能为空");
        }
        if (ids.size() > 100) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "最多支持 100 个 ID");
        }

        // 校验所有任务状态
        List<Task> tasks = lambdaQuery().in(Task::getId, ids).list();
        for (Task task : tasks) {
            if (task.getStatus() == 1) {
                throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED,
                        "所选任务中包含已发布状态的任务，操作不合法：" + task.getTaskName());
            }
        }

        // 批量发布
        lambdaUpdate()
                .in(Task::getId, ids)
                .set(Task::getStatus, 1)
                .update();

        log.info("批量发布任务成功：ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisable(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "ID 列表不能为空");
        }
        if (ids.size() > 100) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "最多支持 100 个 ID");
        }

        // 校验所有任务状态
        List<Task> tasks = lambdaQuery().in(Task::getId, ids).list();
        for (Task task : tasks) {
            if (task.getStatus() != 1) {
                throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED,
                        "所选任务中包含未发布或已停用状态的任务，操作不合法：" + task.getTaskName());
            }
        }

        // 批量停用
        lambdaUpdate()
                .in(Task::getId, ids)
                .set(Task::getStatus, 2)
                .update();

        log.info("批量停用任务成功：ids={}", ids);
    }

    // ==================== 执行管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskTestRunVo testRun(Long id, TaskTestRunRequest request) {
        Task task = getTaskOrThrow(id);

        // 1. 创建执行记录
        TaskExecution execution = new TaskExecution();
        execution.setTaskId(id);
        execution.setStatus(1); // 执行中
        execution.setTriggerType("TEST");
        execution.setStartTime(LocalDateTime.now());
        execution.setDebugMode(request.getDebugMode() != null && request.getDebugMode() ? 1 : 0);
        if (request.getTaskParams() != null) {
            try {
                execution.setTaskParams(objectMapper.writeValueAsString(request.getTaskParams()));
            } catch (Exception e) {
                log.warn("序列化任务参数失败", e);
            }
        }
        taskExecutionMapper.insert(execution);

        // 2. 获取 DAG 节点和连线
        List<TaskNode> nodes = taskNodeMapper.selectList(
                new LambdaQueryWrapper<TaskNode>().eq(TaskNode::getTaskId, id));
        List<TaskEdge> edges = taskEdgeMapper.selectList(
                new LambdaQueryWrapper<TaskEdge>().eq(TaskEdge::getTaskId, id));

        // 3. 按拓扑排序执行
        List<NodeResultVo> nodeResults = executeDag(nodes, edges, execution.getId(), id);

        // 4. 更新执行记录状态
        boolean allSuccess = nodeResults.stream().allMatch(r -> r.getStatus() == 2);
        execution.setStatus(allSuccess ? 2 : 3); // 成功/失败
        execution.setEndTime(LocalDateTime.now());
        execution.setTotalDuration(nodeResults.stream().mapToLong(r -> r.getDuration() != null ? r.getDuration() : 0).sum());
        taskExecutionMapper.updateById(execution);

        // 5. 组装结果
        TaskTestRunVo result = new TaskTestRunVo();
        result.setExecutionId(execution.getId());
        result.setStatus(execution.getStatus());
        result.setStartTime(execution.getStartTime() != null ? execution.getStartTime().format(DTF) : null);
        result.setEndTime(execution.getEndTime() != null ? execution.getEndTime().format(DTF) : null);
        result.setTotalDuration(execution.getTotalDuration());
        result.setNodeResults(nodeResults);

        log.info("测试运行任务完成：taskId={}, executionId={}, status={}", id, execution.getId(), execution.getStatus());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long executeTask(Long id, TaskExecuteRequest request) {
        Task task = getTaskOrThrow(id);
        if (task.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅已发布状态的任务可执行");
        }

        // 创建执行记录
        TaskExecution execution = new TaskExecution();
        execution.setTaskId(id);
        execution.setStatus(0); // 等待
        execution.setTriggerType("MANUAL");
        execution.setStartTime(LocalDateTime.now());
        if (request != null && request.getTaskParams() != null) {
            try {
                execution.setTaskParams(objectMapper.writeValueAsString(request.getTaskParams()));
            } catch (Exception e) {
                log.warn("序列化任务参数失败", e);
            }
        }
        taskExecutionMapper.insert(execution);

        log.info("手动执行任务：taskId={}, executionId={}", id, execution.getId());
        return execution.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExecution(Long taskId, Long executionId) {
        TaskExecution execution = taskExecutionMapper.selectById(executionId);
        if (execution == null || !execution.getTaskId().equals(taskId)) {
            throw new BusinessException(StatusCode.NOT_FOUND, "执行记录不存在");
        }
        if (execution.getStatus() != 0 && execution.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "该执行记录当前状态不允许取消");
        }

        execution.setStatus(4); // 已取消
        execution.setEndTime(LocalDateTime.now());
        taskExecutionMapper.updateById(execution);

        log.info("停止任务执行成功：taskId={}, executionId={}", taskId, executionId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取任务，不存在则抛出异常
     */
    private Task getTaskOrThrow(Long id) {
        Task task = lambdaQuery()
                .eq(Task::getId, id)
                .one();
        if (task == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "任务不存在");
        }
        return task;
    }

    /**
     * 将 Task 实体转换为 TaskListVo
     */
    private TaskListVo convertToListVo(Task task) {
        TaskListVo vo = new TaskListVo();
        vo.setId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setTaskDescription(task.getTaskDescription());
        vo.setCategoryId(task.getCategoryId());
        vo.setStatus(task.getStatus());
        vo.setExecuteStatus(task.getExecuteStatus());
        vo.setScheduleType(task.getScheduleType());
        vo.setLastExecuteTime(task.getLastExecuteTime() != null ? task.getLastExecuteTime().format(DTF) : null);
        vo.setNextExecuteTime(task.getNextExecuteTime() != null ? task.getNextExecuteTime().format(DTF) : null);
        vo.setCreateTime(task.getCreateTime() != null ? task.getCreateTime().format(DTF) : null);
        vo.setUpdateTime(task.getUpdateTime() != null ? task.getUpdateTime().format(DTF) : null);
        return vo;
    }

    /**
     * 将 Task 实体转换为 TaskDetailVo
     */
    private TaskDetailVo convertToDetailVo(Task task) {
        TaskDetailVo vo = new TaskDetailVo();
        vo.setId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setTaskDescription(task.getTaskDescription());
        vo.setCategoryId(task.getCategoryId());
        vo.setStatus(task.getStatus());
        vo.setExecuteStatus(task.getExecuteStatus());
        vo.setScheduleType(task.getScheduleType());
        vo.setLastExecuteTime(task.getLastExecuteTime() != null ? task.getLastExecuteTime().format(DTF) : null);
        vo.setNextExecuteTime(task.getNextExecuteTime() != null ? task.getNextExecuteTime().format(DTF) : null);
        vo.setCreateTime(task.getCreateTime() != null ? task.getCreateTime().format(DTF) : null);
        vo.setUpdateTime(task.getUpdateTime() != null ? task.getUpdateTime().format(DTF) : null);
        vo.setCreateBy(task.getCreateBy());
        vo.setUpdateBy(task.getUpdateBy());
        return vo;
    }

    /**
     * 将 TaskNode 实体转换为 NodeDetailVo
     */
    private TaskDetailVo.NodeDetailVo convertToNodeDetail(TaskNode node) {
        TaskDetailVo.NodeDetailVo vo = new TaskDetailVo.NodeDetailVo();
        vo.setId(node.getId());
        vo.setNodeId(node.getNodeId());
        vo.setNodeName(node.getNodeName());
        vo.setNodeType(node.getNodeType());
        vo.setPositionX(node.getPositionX());
        vo.setPositionY(node.getPositionY());
        vo.setNodeConfig(node.getNodeConfig());
        vo.setCreateTime(node.getCreateTime() != null ? node.getCreateTime().format(DTF) : null);
        vo.setUpdateTime(node.getUpdateTime() != null ? node.getUpdateTime().format(DTF) : null);
        return vo;
    }

    /**
     * 构建触发配置 VO
     */
    private TaskDetailVo.TriggerConfigVo buildTriggerConfig(Task task) {
        TaskDetailVo.TriggerConfigVo config = new TaskDetailVo.TriggerConfigVo();
        config.setScheduleType(task.getScheduleType());
        config.setCronExpression(task.getCronExpression());
        config.setEffectiveDate(task.getEffectiveDate() != null ? task.getEffectiveDate().format(DTF) : null);
        config.setExpireDate(task.getExpireDate() != null ? task.getExpireDate().format(DTF) : null);
        return config;
    }

    /**
     * 将 TaskExecution 实体转换为 TaskExecutionVo
     */
    private TaskExecutionVo convertToExecutionVo(TaskExecution execution) {
        TaskExecutionVo vo = new TaskExecutionVo();
        vo.setExecutionId(execution.getId());
        vo.setStatus(execution.getStatus());
        vo.setStartTime(execution.getStartTime() != null ? execution.getStartTime().format(DTF) : null);
        vo.setEndTime(execution.getEndTime() != null ? execution.getEndTime().format(DTF) : null);
        vo.setDuration(execution.getTotalDuration());
        vo.setTriggerType(execution.getTriggerType());
        vo.setTriggerBy(execution.getTriggerBy());
        return vo;
    }

    /**
     * 执行 DAG（拓扑排序后逐节点执行）
     *
     * 简化实现：按连线关系找出拓扑顺序，依次执行每个节点
     */
    private List<NodeResultVo> executeDag(List<TaskNode> nodes, List<TaskEdge> edges,
                                           Long executionId, Long taskId) {
        List<NodeResultVo> results = new ArrayList<>();

        // 按拓扑排序执行节点
        List<String> sortedNodeIds = topologicalSort(nodes, edges);

        // 节点ID -> 节点的映射
        Map<String, TaskNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(TaskNode::getNodeId, n -> n));

        for (String nodeId : sortedNodeIds) {
            TaskNode node = nodeMap.get(nodeId);
            if (node == null) continue;

            NodeResultVo nodeResult = new NodeResultVo();
            nodeResult.setNodeId(node.getNodeId());
            nodeResult.setNodeName(node.getNodeName());
            nodeResult.setNodeType(node.getNodeType());
            nodeResult.setStartTime(LocalDateTime.now().format(DTF));
            nodeResult.setLogs("");

            long startMs = System.currentTimeMillis();
            try {
                // 模拟执行节点（实际应调用具体执行器）
                String nodeType = node.getNodeType();
                switch (nodeType) {
                    case "START":
                        nodeResult.setOutputData("{}");
                        nodeResult.setLogs("Start node executed successfully.");
                        break;
                    case "END":
                        nodeResult.setInputData("{}");
                        nodeResult.setLogs("End node executed successfully.");
                        break;
                    case "API":
                        nodeResult.setInputData("{}");
                        nodeResult.setOutputData("{\"status\": 200}");
                        nodeResult.setLogs("API node executed successfully.");
                        break;
                    case "SCRIPT":
                        nodeResult.setInputData("{}");
                        nodeResult.setOutputData("{}");
                        nodeResult.setLogs("Script node executed successfully.");
                        break;
                    case "MAPPING":
                        nodeResult.setInputData("{}");
                        nodeResult.setOutputData("{}");
                        nodeResult.setLogs("Mapping node executed successfully.");
                        break;
                    case "OUTPUT":
                        nodeResult.setInputData("{}");
                        nodeResult.setLogs("Output node executed successfully.");
                        break;
                    default:
                        nodeResult.setLogs("Unknown node type: " + nodeType);
                }
                nodeResult.setStatus(2); // 成功

                // 保存节点执行记录
                saveNodeExecution(executionId, taskId, node, nodeResult, null);
            } catch (Exception e) {
                long endMs = System.currentTimeMillis();
                nodeResult.setStatus(3); // 失败
                nodeResult.setDuration(endMs - startMs);
                nodeResult.setEndTime(LocalDateTime.now().format(DTF));
                nodeResult.setErrorMessage(e.getMessage());

                saveNodeExecution(executionId, taskId, node, nodeResult, e.getMessage());
                results.add(nodeResult);
                // 节点失败，后续节点跳过
                break;
            }

            long endMs = System.currentTimeMillis();
            nodeResult.setDuration(endMs - startMs);
            nodeResult.setEndTime(LocalDateTime.now().format(DTF));

            // 如果日志为空，设置默认日志
            if (nodeResult.getLogs() == null || nodeResult.getLogs().isEmpty()) {
                nodeResult.setLogs("Node " + node.getNodeType() + " executed.");
            }

            results.add(nodeResult);
        }

        // 未执行的节点设为跳过
        Set<String> executedIds = results.stream()
                .map(NodeResultVo::getNodeId)
                .collect(Collectors.toSet());
        for (TaskNode node : nodes) {
            if (!executedIds.contains(node.getNodeId())) {
                NodeResultVo skipped = new NodeResultVo();
                skipped.setNodeId(node.getNodeId());
                skipped.setNodeName(node.getNodeName());
                skipped.setNodeType(node.getNodeType());
                skipped.setStatus(5); // 跳过
                skipped.setLogs("Node skipped due to upstream failure.");
                results.add(skipped);
            }
        }

        return results;
    }

    /**
     * 保存节点执行记录
     */
    private void saveNodeExecution(Long executionId, Long taskId, TaskNode node,
                                    NodeResultVo nodeResult, String errorMessage) {
        TaskNodeExecution nodeExec = new TaskNodeExecution();
        nodeExec.setExecutionId(executionId);
        nodeExec.setTaskId(taskId);
        nodeExec.setNodeId(node.getNodeId());
        nodeExec.setNodeName(node.getNodeName());
        nodeExec.setNodeType(node.getNodeType());
        nodeExec.setStatus(nodeResult.getStatus());
        nodeExec.setStartTime(LocalDateTime.now());
        nodeExec.setInputData(nodeResult.getInputData());
        nodeExec.setOutputData(nodeResult.getOutputData());
        nodeExec.setErrorMessage(errorMessage);
        nodeExec.setLogs(nodeResult.getLogs());
        taskNodeExecutionMapper.insert(nodeExec);
    }

    /**
     * 拓扑排序
     */
    private List<String> topologicalSort(List<TaskNode> nodes, List<TaskEdge> edges) {
        // 构建入度表和邻接表
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjacency = new HashMap<>();

        for (TaskNode node : nodes) {
            inDegree.put(node.getNodeId(), 0);
            adjacency.put(node.getNodeId(), new ArrayList<>());
        }

        for (TaskEdge edge : edges) {
            adjacency.get(edge.getSourceNodeId()).add(edge.getTargetNodeId());
            inDegree.merge(edge.getTargetNodeId(), 1, Integer::sum);
        }

        // Kahn 算法
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<String> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            sorted.add(nodeId);
            for (String neighbor : adjacency.getOrDefault(nodeId, new ArrayList<>())) {
                inDegree.merge(neighbor, -1, Integer::sum);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return sorted;
    }

    /**
     * 将 ScheduleConfig 转换为 Cron 表达式
     *
     * 简化实现：生成常见的 Cron 表达式
     */
    private String buildCronExpression(ScheduleConfig config) {
        if (config == null) {
            return null;
        }

        String timeUnit = config.getTimeUnit();
        String configType = config.getConfigType();

        // 简化实现 - 根据配置类型生成 Cron
        if ("SECOND".equals(timeUnit)) {
            if ("INTERVAL".equals(configType) && config.getIntervalValue() != null && config.getFromValue() != null) {
                return config.getFromValue() + "/" + config.getIntervalValue() + " * * * * ?";
            } else if ("SPECIFY".equals(configType) && config.getSpecifiedValues() != null) {
                String sec = config.getSpecifiedValues().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                return sec + " * * * * ?";
            } else if ("RANGE".equals(configType) && config.getFromValue() != null && config.getToValue() != null) {
                return config.getFromValue() + "-" + config.getToValue() + " * * * * ?";
            }
        } else if ("MINUTE".equals(timeUnit)) {
            if ("INTERVAL".equals(configType) && config.getIntervalValue() != null) {
                return "0 " + (config.getFromValue() != null ? config.getFromValue() + "/" + config.getIntervalValue() : "*/" + config.getIntervalValue()) + " * * * ?";
            }
        } else if ("HOUR".equals(timeUnit)) {
            if ("INTERVAL".equals(configType) && config.getIntervalValue() != null) {
                return "0 0 " + (config.getFromValue() != null ? config.getFromValue() + "/" + config.getIntervalValue() : "*/" + config.getIntervalValue()) + " * * ?";
            }
        } else if ("DAY".equals(timeUnit)) {
            return "0 0 " + (config.getFromValue() != null ? config.getFromValue() : "2") + " * * ?";
        }

        // 默认每天凌晨2点
        return "0 0 2 * * ?";
    }
}
