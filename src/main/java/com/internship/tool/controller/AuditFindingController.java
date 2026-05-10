package com.internship.tool.controller;

import com.internship.tool.dto.DashboardStatsDto;
import com.internship.tool.entity.AuditFinding;
import com.internship.tool.service.AuditFindingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/findings")
@RequiredArgsConstructor
@Tag(name = "Audit Findings", description = "Management endpoints for system audit findings and report generation")
public class AuditFindingController {

    private final AuditFindingService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @Operation(summary = "Create finding", description = "Registers a new audit finding. Restricted to ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Finding created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<AuditFinding> create(@RequestBody AuditFinding finding) {
        return ResponseEntity.ok(service.createFinding(finding));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all findings", description = "Retrieves a paginated and sorted list of all findings. Open to all authenticated users.")
    @ApiResponse(responseCode = "200", description = "Page of findings retrieved")
    public ResponseEntity<Page<AuditFinding>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(service.getAllFindings(page, size, sortBy, sortDir));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search findings", description = "Full-text search across finding titles and descriptions using a keyword.")
    @ApiResponse(responseCode = "200", description = "Search results returned")
    public ResponseEntity<Page<AuditFinding>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(service.searchFindings(q, page, size, sortBy, sortDir));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "View dashboard metrics", description = "Aggregated statistics for charts and dashboard KPIs (ADMIN/MANAGER only).")
    @ApiResponse(responseCode = "200", description = "Stats calculated and returned")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(service.getDashboardStats());
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Download CSV", description = "Generates and downloads a CSV report of all findings.")
    @ApiResponse(responseCode = "200", description = "CSV stream initiated")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        String csvContent = service.exportFindingsAsCsv(sortBy, sortDir);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-findings.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get single finding", description = "Retrieves detailed information for a specific finding by its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Finding found"),
            @ApiResponse(responseCode = "404", description = "Finding not found with provided ID")
    })
    public ResponseEntity<AuditFinding> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getFindingById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @Operation(summary = "Update finding", description = "Modifies an existing finding. Note: This triggers an audit log entry.")
    @ApiResponse(responseCode = "200", description = "Update successful")
    public ResponseEntity<AuditFinding> update(@PathVariable Long id, @RequestBody AuditFinding finding) {
        return ResponseEntity.ok(service.updateFinding(id, finding));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @Operation(summary = "Soft delete finding", description = "Marks a finding as inactive without removing it from the database (ADMIN only).")
    @ApiResponse(responseCode = "204", description = "No Content - Successfully deleted")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDeleteFinding(id);
        return ResponseEntity.noContent().build();
    }
}
