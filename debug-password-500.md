# Debug Session: `password-500`

## Symptom
当测试修改密码接口 (`PUT /api/v1/auth/password`) 时，返回 HTTP 500 错误。

## Hypotheses

### H1: `MissingRequestHeaderException` 未处理
`@RequestHeader("Authorization")` 默认 required=true，头缺失时抛出 `MissingServletRequestPartException` 或 `MissingRequestHeaderException`，该异常未被 `GlobalExceptionHandler` 捕获，落入兜底的 `Exception` 处理器返回 500。

### H2: JWT 令牌解析异常未处理
如果请求头携带了 Authorization 令牌但令牌格式无效（如过期、被篡改），`jwtUtils.validateToken()` 或 `jwtUtils.getUserIdFromToken()` 可能抛出未捕获的异常（如 `ExpiredJwtException`, `SignatureException`, `MalformedJwtException` 等），触发通用 500 处理器。

### H3: `BusinessException(PASSWORD_SAME_AS_OLD)` 的序列化问题
当新旧密码相同时，抛出 `BusinessException(StatusCode.PASSWORD_SAME_AS_OLD, ...)`，但 `@ExceptionHandler(BusinessException.class)` 返回的 `Result` 可能由于泛型问题导致序列化异常。

### H4: Controller 参数绑定异常
`@Valid @RequestBody` + `@RequestHeader` 同时使用时，参数绑定时可能抛出未处理的 `HttpMessageNotReadableException` 等异常，落入通用处理器。

## Plan
1. ✅ Add instrumentation in GlobalExceptionHandler to log which exception is actually triggered
2. ✅ Ask user to reproduce the issue → Confirmed: `MissingRequestHeaderException`
3. ✅ Apply minimal fix: Added `@ExceptionHandler(MissingRequestHeaderException.class)` handler
4. 🔄 Ask user to verify post-fix
