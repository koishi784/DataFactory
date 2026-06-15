package com.datafactory.core.executor;

import com.datafactory.common.model.vo.script.ScriptDebugVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Python 脚本执行器
 *
 * 通过 ProcessBuilder 外部进程方式执行 Python 脚本，支持超时控制。
 * 后续可替换为 Docker 容器执行以获得更好的安全隔离。
 */
@Slf4j
@Component("PYTHON")
public class PythonScriptExecutor implements ScriptExecutor {

    /** Python 脚本执行超时时间（秒） */
    private static final long TIMEOUT_SECONDS = 60;

    /** Python 解释器路径，默认使用系统 PATH 中的 python3 */
    @Value("${script.python.command:python}")
    private String pythonCommand;

    @Override
    public ScriptDebugVo execute(String scriptContent, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        ScriptDebugVo result = new ScriptDebugVo();

        // 1. 将脚本内容写入临时文件
        File tempScript = null;
        try {
            tempScript = File.createTempFile("script_debug_", ".py");
            try (FileWriter writer = new FileWriter(tempScript, StandardCharsets.UTF_8)) {
                writer.write(scriptContent);
            }

            // 2. 构建命令行参数
            ProcessBuilder pb = new ProcessBuilder(pythonCommand, tempScript.getAbsolutePath());

            // 3. 传递参数到环境变量（脚本内通过 os.environ 读取）
            if (params != null && !params.isEmpty()) {
                Map<String, String> env = pb.environment();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    env.put("SCRIPT_PARAM_" + entry.getKey(),
                            entry.getValue() != null ? entry.getValue().toString() : "");
                }
            }

            // 4. 合并错误流到输出流
            pb.redirectErrorStream(true);

            // 5. 在独立线程中执行，支持超时
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Process process = pb.start();

            Future<String> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString().trim();
                }
            });

            String output = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            int exitCode = process.waitFor();
            executor.shutdownNow();

            long cost = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                result.setSuccess(true);
                result.setExecuteTime(cost);
                result.setResult(output);
                result.setErrorMessage(null);
                log.info("Python 脚本执行成功，耗时: {}ms", cost);
            } else {
                result.setSuccess(false);
                result.setExecuteTime(cost);
                result.setResult(null);
                result.setErrorMessage("脚本退出码: " + exitCode + "，输出: " + output);
                log.warn("Python 脚本执行失败，退出码: {}，耗时: {}ms", exitCode, cost);
            }

        } catch (TimeoutException e) {
            long cost = System.currentTimeMillis() - startTime;
            result.setSuccess(false);
            result.setExecuteTime(cost);
            result.setErrorMessage("脚本执行超时（超过 " + TIMEOUT_SECONDS + " 秒）");
            log.warn("Python 脚本执行超时，耗时: {}ms", cost);
        } catch (IOException e) {
            long cost = System.currentTimeMillis() - startTime;
            result.setSuccess(false);
            result.setExecuteTime(cost);
            result.setErrorMessage("无法启动 Python 解释器: " + e.getMessage());
            log.error("Python 解释器启动失败", e);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            result.setSuccess(false);
            result.setExecuteTime(cost);
            result.setErrorMessage(e.getMessage());
            log.error("Python 脚本执行异常，耗时: {}ms", cost, e);
        } finally {
            // 清理临时文件
            if (tempScript != null && tempScript.exists()) {
                tempScript.delete();
            }
        }

        return result;
    }
}
