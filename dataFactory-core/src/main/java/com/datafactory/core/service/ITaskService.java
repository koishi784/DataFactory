package com.datafactory.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.datafactory.common.model.dto.task.*;
import com.datafactory.common.model.vo.task.*;
import com.datafactory.core.domain.entity.Task;

import java.util.List;

/**
 * 任务管理服务接口
 *
 * 提供任务的 CRUD、DAG 编排、触发配置、执行管理、状态变更等业务操作
 */
public interface ITaskService extends IService<Task> {

    // ==================== 查询 ====================

    /**
     * 分页查询任务列表
     *
     * 支持多条件筛选：关键词、状态、分类、执行状态
     * 排序规则：优先级一按状态（DRAFT→PUBLISHED→DISABLED），优先级二按更新时间倒序
     *
     * @param pageNum      页码，默认 1
     * @param pageSize     每页条数，默认 20
     * @param keyword      关键词，模糊匹配任务名称、任务说明
     * @param status       发布状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @param categoryId   分类ID筛选（查询该分类及其所有后代分类下的任务）
     * @param executeStatus 最近执行状态筛选：0=等待 / 1=执行中 / 2=成功 / 3=失败 / 4=已取消
     * @return 分页结果
     */
    IPage<TaskListVo> getTaskPage(Integer pageNum, Integer pageSize,
                                  String keyword, String status,
                                  Long categoryId, Integer executeStatus);

    /**
     * 查询任务详情
     *
     * 包含任务基本信息、DAG 节点和连线、触发配置等完整信息
     *
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDetailVo getTaskDetail(Long id);

    /**
     * 分页查询任务执行历史
     *
     * @param taskId   任务ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate   结束日期（yyyy-MM-dd）
     * @param status    执行状态筛选，多值用逗号分隔
     * @return 分页结果
     */
    IPage<TaskExecutionVo> getExecutionHistory(Long taskId, Integer pageNum, Integer pageSize,
                                                String startDate, String endDate, String status);

    // ==================== 新增/编辑 ====================

    /**
     * 新增任务（基本信息）
     *
     * 创建任务的基本信息（三步向导第一步），返回 taskId
     * 创建后状态为 DRAFT
     *
     * @param request 新增任务请求参数
     * @return 创建的任务实体
     */
    Task createTask(TaskCreateRequest request);

    /**
     * 更新任务 DAG 配置
     *
     * 配置任务 DAG 流程（三步向导第二步），包含节点和连线
     * 仅未发布(0)和已停用(2)状态可配置
     *
     * @param id      任务ID
     * @param request DAG 配置请求参数
     */
    void updateTaskConfig(Long id, TaskConfigRequest request);

    /**
     * 任务触发设置
     *
     * 配置任务触发方式（三步向导第三步）
     * 支持 API 触发和定时任务两种方式
     * 仅任务流程测试通过后才可进入此步骤
     *
     * @param id      任务ID
     * @param request 触发设置请求参数
     */
    void updateTriggerConfig(Long id, TaskTriggerConfigRequest request);

    // ==================== 状态变更 ====================

    /**
     * 发布任务
     *
     * 将未发布(0)或已停用(2)状态的任务发布为已发布(1)
     *
     * @param id 任务ID
     */
    void publish(Long id);

    /**
     * 停用任务
     *
     * 将已发布(1)状态的任务停用为已停用(2)
     *
     * @param id 任务ID
     */
    void disable(Long id);

    /**
     * 删除任务
     *
     * 仅可删除未发布(0)状态的任务
     *
     * @param id 任务ID
     */
    void deleteTask(Long id);

    /**
     * 批量发布任务
     *
     * 所选任务须全部为未发布(0)或已停用(2)状态，不能包含已发布(1)状态
     *
     * @param ids 任务ID列表
     */
    void batchPublish(List<Long> ids);

    /**
     * 批量停用任务
     *
     * 所选任务须全部为已发布(1)状态，不能包含未发布(0)或已停用(2)状态
     *
     * @param ids 任务ID列表
     */
    void batchDisable(List<Long> ids);

    // ==================== 执行管理 ====================

    /**
     * 测试运行任务
     *
     * 在线测试运行指定任务的 DAG 流程，按拓扑排序逐节点执行
     * 返回各节点的执行日志和结果
     *
     * @param id      任务ID
     * @param request 测试运行请求参数
     * @return 测试运行结果
     */
    TaskTestRunVo testRun(Long id, TaskTestRunRequest request);

    /**
     * 手动执行任务
     *
     * 立即执行指定任务（仅已发布状态的任务可执行）
     *
     * @param id      任务ID
     * @param request 手动执行请求参数
     * @return 执行记录ID
     */
    Long executeTask(Long id, TaskExecuteRequest request);

    /**
     * 停止正在执行的任务
     *
     * @param taskId      任务ID
     * @param executionId 执行记录ID
     */
    void cancelExecution(Long taskId, Long executionId);
}
