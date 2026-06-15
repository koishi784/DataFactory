package com.datafactory.core.executor;

import com.datafactory.common.model.vo.script.ScriptDebugVo;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Groovy 脚本执行器
 *
 * 在 JVM 内通过 GroovyShell 执行 Groovy 脚本，支持沙箱安全限制和超时控制。
 * 每次执行创建独立的 GroovyShell 实例，避免状态污染。
 * 自动注入 JdbcTemplate 和业务参数，脚本中可直接使用。
 */
@Slf4j
@Component("GROOVY")
public class GroovyScriptExecutor implements ScriptExecutor {

    /** Groovy 脚本执行超时时间（秒） */
    private static final long TIMEOUT_SECONDS = 30;

    /** 安全编译配置（复用，线程安全） */
    private final CompilerConfiguration secureConfig;

    /** JdbcTemplate，注入到 Groovy 脚本中供数据清洗使用 */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 初始化安全编译配置，接收 Spring 注入的 JdbcTemplate
     */
    public GroovyScriptExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        SecureASTCustomizer secure = new SecureASTCustomizer();

        // 禁止导入危险类，防止脚本破坏系统
        secure.setImportsBlacklist(java.util.Arrays.asList(
                "java.lang.Runtime",
                "java.lang.ProcessBuilder",
                "java.lang.System",
                "java.io.File",
                "java.io.FileInputStream",
                "java.io.FileOutputStream",
                "java.io.FileWriter",
                "java.io.FileReader",
                "java.net.Socket",
                "java.net.ServerSocket",
                "java.net.URL",
                "java.net.HttpURLConnection",
                "javax.script.ScriptEngineManager"
        ));

        // 禁止使用静态星号导入（如 import static java.lang.System.*）
        secure.setStaticStarImportsBlacklist(java.util.Arrays.asList(
                "java.lang.System",
                "java.lang.Runtime"
        ));

        // 允许在脚本中定义方法
        secure.setMethodDefinitionAllowed(true);

        this.secureConfig = new CompilerConfiguration();
        this.secureConfig.addCompilationCustomizers(secure);
    }

    @Override
    public ScriptDebugVo execute(String scriptContent, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        ScriptDebugVo result = new ScriptDebugVo();

        // 每次执行创建独立的 GroovyShell，避免状态污染
        GroovyShell shell = new GroovyShell(secureConfig);

        try {
            // 1. 注入 JdbcTemplate，脚本中可通过变量名 jdbcTemplate 直接使用
            shell.setVariable("jdbcTemplate", jdbcTemplate);

            // 2. 设置业务参数到 Binding
            if (params != null && !params.isEmpty()) {
                params.forEach(shell::setVariable);
            }

            // 3. 在独立线程中执行，支持超时控制
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Object> future = executor.submit(() -> shell.evaluate(scriptContent));

            Object evalResult = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.shutdownNow();

            long cost = System.currentTimeMillis() - startTime;
            result.setSuccess(true);
            result.setExecuteTime(cost);
            result.setResult(evalResult != null ? evalResult.toString() : "null");
            result.setErrorMessage(null);

            log.info("Groovy 脚本执行成功，耗时: {}ms", cost);
        } catch (TimeoutException e) {
            long cost = System.currentTimeMillis() - startTime;
            result.setSuccess(false);
            result.setExecuteTime(cost);
            result.setResult(null);
            result.setErrorMessage("脚本执行超时（超过 " + TIMEOUT_SECONDS + " 秒）");
            log.warn("Groovy 脚本执行超时，耗时: {}ms", cost);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            result.setSuccess(false);
            result.setExecuteTime(cost);
            result.setResult(null);
            result.setErrorMessage(e.getMessage());
            log.error("Groovy 脚本执行失败，耗时: {}ms", cost, e);
        } finally {
            // GroovyShell 无需要手动释放，由 JVM GC 自动回收
        }

        return result;
    }
}
