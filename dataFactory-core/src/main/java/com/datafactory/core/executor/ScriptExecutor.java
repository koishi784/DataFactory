package com.datafactory.core.executor;

import com.datafactory.common.model.vo.script.ScriptDebugVo;

import java.util.Map;

/**
 * 脚本执行器接口
 *
 * 定义脚本调试执行的统一契约，不同脚本类型（GROOVY、PYTHON）通过不同的实现类执行。
 */
public interface ScriptExecutor {

    /**
     * 执行脚本
     *
     * @param scriptContent 脚本文件内容
     * @param params        执行参数（键值对）
     * @return 调试执行结果
     */
    ScriptDebugVo execute(String scriptContent, Map<String, Object> params);
}
