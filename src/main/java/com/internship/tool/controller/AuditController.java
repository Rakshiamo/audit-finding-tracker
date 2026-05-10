package com.internship.tool.controller;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Controller", description = "Endpoints for viewing system change logs and history")
public class AuditController {

    private final AuditLogService auditService;

    @Operation(summary = "Get all audit logs", description = "Retrieves a complete list of all changes made to system entities.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved logs")
    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditService.findAll());
    }

    @Operation(summary = "Get logs by entity", description = "Fetches history for a specific entity type (e.g., 'FINDING').")
    @ApiResponse(responseCode = "200", description = "Logs found")
    @ApiResponse(responseCode = "404", description = "No logs found for this entity")
    @GetMapping("/{entityType}")
    public ResponseEntity<List<AuditLog>> getByEntity(@PathVariable String entityType) {
        return ResponseEntity.ok(auditService.findByEntityType(entityType));
    }
}
