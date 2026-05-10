package com.internship.tool.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.AuditFinding;
import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditFindingRepository;
import com.internship.tool.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggingAspect {

    private final AuditFindingRepository auditFindingRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.internship.tool.controller..*.create*(..))")
    public Object logCreate(ProceedingJoinPoint joinPoint) throws Throwable {

        Object result = joinPoint.proceed();

        AuditFinding created = extractFinding(result);

        if (created != null) {

            saveAudit(
                    "CREATE",
                    created.getId(),
                    null,
                    toJson(created)
            );
        }

        return result;
    }

    @Around("execution(* com.internship.tool.controller..*.update*(..))")
    public Object logUpdate(ProceedingJoinPoint joinPoint) throws Throwable {

        Long id = extractId(joinPoint.getArgs());

        String oldJson = null;

        if (id != null) {

            oldJson = auditFindingRepository
                    .findById(id)
                    .map(this::toJson)
                    .orElse(null);
        }

        Object result = joinPoint.proceed();

        AuditFinding updated = extractFinding(result);

        if (updated != null) {

            saveAudit(
                    "UPDATE",
                    updated.getId(),
                    oldJson,
                    toJson(updated)
            );
        }

        return result;
    }

    @Around("execution(* com.internship.tool.controller..*.delete*(..))")
    public Object logDelete(ProceedingJoinPoint joinPoint) throws Throwable {

        Long id = extractId(joinPoint.getArgs());

        String oldJson = null;

        if (id != null) {

            oldJson = auditFindingRepository
                    .findById(id)
                    .map(this::toJson)
                    .orElse(null);
        }

        Object result = joinPoint.proceed();

        if (id != null) {

            saveAudit(
                    "DELETE",
                    id,
                    oldJson,
                    null
            );
        }

        return result;
    }

    private AuditFinding extractFinding(Object result) {

        if (result instanceof AuditFinding finding) {
            return finding;
        }

        if (result instanceof ResponseEntity<?> response &&
            response.getBody() instanceof AuditFinding finding) {

            return finding;
        }

        return null;
    }

    private Long extractId(Object[] args) {

        if (args == null) {
            return null;
        }

        for (Object arg : args) {

            if (arg instanceof Long value) {
                return value;
            }
        }

        return null;
    }

    private void saveAudit(
            String action,
            Long entityId,
            String oldValue,
            String newValue
    ) {

        if (entityId == null) {
            return;
        }

        AuditLog auditLog = new AuditLog();

        auditLog.setEntityName("AuditFinding");
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setPerformedBy(getCurrentUsername());

        auditLogRepository.save(auditLog);
    }

    private String getCurrentUsername() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication.getName().equals("anonymousUser")) {

            return "SYSTEM";
        }

        return authentication.getName();
    }

    private String toJson(Object value) {

        try {

            return objectMapper.writeValueAsString(value);

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Failed to serialize audit payload",
                    ex
            );
        }
    }
}