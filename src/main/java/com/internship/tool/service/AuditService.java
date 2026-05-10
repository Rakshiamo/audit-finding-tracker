package com.internship.tool.service;

import com.internship.tool.entity.AuditFinding;
import com.internship.tool.repository.AuditFindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditFindingRepository auditFindingRepository;

    public AuditFinding updateAudit(Long id, AuditFinding request) {
        AuditFinding existing = auditFindingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Finding not found"));

        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getSeverity() != null) {
            existing.setSeverity(request.getSeverity());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            existing.setDueDate(request.getDueDate());
        }
        return auditFindingRepository.save(existing);
    }

    public void softDeleteAudit(Long id) {
        AuditFinding existing = auditFindingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Finding not found"));
        existing.setDeletedAt(LocalDateTime.now());
        auditFindingRepository.save(existing);
    }

    public Page<AuditFinding> searchAudit(String q, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        Pageable pageable = PageRequest.of(page, size, sort);

        return auditFindingRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrSeverityContainingIgnoreCaseOrStatusContainingIgnoreCase(
                q, pageable);
    }

    public Map<String, Object> getStats() {
        long total = auditFindingRepository.countTotalFindings();
        long open = auditFindingRepository.countByStatusIgnoreCase("OPEN");
        long closed = auditFindingRepository.countByStatusIgnoreCase("CLOSED");
        long overdue = auditFindingRepository.countByDueDateBeforeAndStatusIgnoreCaseNot(
                LocalDate.now(), "CLOSED");

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("open", open);
        stats.put("closed", closed);
        stats.put("overdue", overdue);
        return stats;
    }

}
