package com.example.elderai.common;

import com.example.elderai.entity.SystemLog;
import com.example.elderai.observability.TraceContext;
import com.example.elderai.security.AuthenticatedUser;
import com.example.elderai.security.ClientIpResolver;
import com.example.elderai.service.AuditLogWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Controller 访问审计。只记录操作元数据，不落库请求正文和敏感业务内容。
 */
@Aspect
@Component
public class WebLogAspect {

    private static final Logger log = LoggerFactory.getLogger(WebLogAspect.class);
    private static final int MAX_ERROR_LENGTH = 500;

    private final AuditLogWriter auditLogWriter;
    private final ClientIpResolver clientIpResolver;

    public WebLogAspect(AuditLogWriter auditLogWriter, ClientIpResolver clientIpResolver) {
        this.auditLogWriter = auditLogWriter;
        this.clientIpResolver = clientIpResolver;
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.GetMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void controllerMethod() {
    }

    @Around("controllerMethod()")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = currentAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        AuthenticatedUser currentUser = currentUser();
        long startedAt = System.currentTimeMillis();
        Throwable failure = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - startedAt;
            int statusCode = resolveStatus(response, failure);
            String username = currentUser == null ? null : currentUser.username();
            String ip = clientIpResolver.resolve(request);

            log.info("[访问审计] method={} uri={} status={} durationMs={} user={} ip={}",
                    request.getMethod(), request.getRequestURI(), statusCode, duration,
                    username == null ? "anonymous" : username, ip);

            SystemLog systemLog = new SystemLog();
            systemLog.setUserId(currentUser == null ? null : currentUser.userId());
            systemLog.setUsername(username);
            systemLog.setUserRole(currentUser == null ? null : currentUser.role());
            systemLog.setOperation(request.getMethod() + " " + request.getRequestURI());
            systemLog.setMethod(joinPoint.getSignature().getDeclaringTypeName()
                    + "." + joinPoint.getSignature().getName());
            systemLog.setParams(describeArguments(joinPoint.getArgs()));
            systemLog.setIp(ip);
            systemLog.setDuration(duration);
            systemLog.setTraceId(resolveTraceId(request));
            systemLog.setStatusCode(statusCode);
            systemLog.setSuccess(statusCode < 400 ? 1 : 0);
            systemLog.setErrorMessage(errorMessage(failure));
            systemLog.setCreateTime(LocalDateTime.now());
            auditLogWriter.write(systemLog);
        }
    }

    private ServletRequestAttributes currentAttributes() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes;
        }
        return null;
    }

    private AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = TraceContext.currentTraceId();
        if (traceId != null) {
            return traceId;
        }
        Object attribute = request.getAttribute(TraceContext.TRACE_ID_ATTRIBUTE);
        return attribute == null ? null : attribute.toString();
    }

    private int resolveStatus(HttpServletResponse response, Throwable failure) {
        if (failure instanceof BusinessException businessException) {
            int code = businessException.getCode();
            return code >= 100 && code <= 599 ? code : 500;
        }
        if (failure != null) {
            return 500;
        }
        return response == null ? 200 : response.getStatus();
    }

    private String describeArguments(Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return "[]";
        }
        StringBuilder result = new StringBuilder("[");
        for (Object argument : arguments) {
            if (argument instanceof HttpServletRequest || argument instanceof HttpServletResponse) {
                continue;
            }
            if (result.length() > 1) {
                result.append(", ");
            }
            result.append(describeArgument(argument));
        }
        return result.append(']').toString();
    }

    private String describeArgument(Object argument) {
        if (argument == null) {
            return "null";
        }
        if (argument instanceof Number || argument instanceof Boolean || argument instanceof Enum<?>
                || argument instanceof LocalDate || argument instanceof LocalTime) {
            return argument.toString();
        }
        if (argument instanceof MultipartFile file) {
            return "MultipartFile(size=" + file.getSize() + ", type=" + safeContentType(file.getContentType()) + ")";
        }
        return argument.getClass().getSimpleName() + "(内容已脱敏)";
    }

    private String safeContentType(String contentType) {
        if (contentType == null || contentType.length() > 100 || !contentType.matches("[A-Za-z0-9.+/-]+")) {
            return "unknown";
        }
        return contentType;
    }

    private String errorMessage(Throwable failure) {
        if (failure == null) {
            return null;
        }
        String message = failure instanceof BusinessException businessException
                ? "BusinessException(" + businessException.getCode() + ")"
                : failure.getClass().getSimpleName();
        if (message == null) {
            return "处理失败";
        }
        return message.length() <= MAX_ERROR_LENGTH ? message : message.substring(0, MAX_ERROR_LENGTH);
    }
}
