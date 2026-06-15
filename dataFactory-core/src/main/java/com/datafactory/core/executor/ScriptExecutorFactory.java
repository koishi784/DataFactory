package com.datafactory.core.executor;

import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.enums.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 脚本执行器工厂
 *
 * 根据脚本类型（GROOVY / PYTHON）路由到对应的 ScriptExecutor 实现。
 */
@Component
@RequiredArgsConstructor
public class ScriptExecutorFactory {

    private final Map<String, ScriptExecutor> executorMap;

    /**
     * 根据脚本类型获取对应的执行器
     *
     * @param scriptType 脚本类型（GROOVY / PYTHON）
     * @return 脚本执行器
     */
    public ScriptExecutor getExecutor(String scriptType) {
        ScriptExecutor executor = executorMap.get(scriptType);
        if (executor == null) {
            throw new BusinessException(StatusCode.BAD_REQUEST,
                    "不支持的脚本类型: " + scriptType + "，仅支持 GROOVY / PYTHON");
        }
        return executor;
    }
}
