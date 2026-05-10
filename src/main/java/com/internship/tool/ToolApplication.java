package com.internship.tool;

import com.internship.tool.entity.AuditFinding;
import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditFindingRepository;
import com.internship.tool.repository.AuditLogRepository;
import com.internship.tool.service.AuditFindingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class ToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToolApplication.class, args);
	}

	@Bean
	CommandLineRunner seedDemoFindings(
			AuditFindingRepository auditFindingRepository,
			AuditFindingService auditFindingService,
			AuditLogRepository auditLogRepository
	) {
		return args -> {
			if (auditFindingRepository.countTotalFindings() > 0) {
				return;
			}

			List<AuditFinding> demoFindings = List.of(
					buildFinding("Expired TLS certificate on vendor portal", "Certificate renewal process failed for the external supplier portal.", "HIGH", "OPEN", 3),
					buildFinding("Inactive privileged accounts not removed", "Former administrators still retain active elevated access in production.", "CRITICAL", "OPEN", 5),
					buildFinding("Quarterly backup restore test overdue", "The latest disaster recovery restore drill was not completed within policy timeline.", "HIGH", "IN_PROGRESS", 7),
					buildFinding("Shared finance mailbox lacks MFA", "Accounts team shared mailbox can be accessed without multifactor authentication.", "CRITICAL", "OPEN", 2),
					buildFinding("Firewall rule review incomplete", "Legacy allow rules were not revalidated during the last network review cycle.", "MEDIUM", "OPEN", 14),
					buildFinding("PII retention schedule not enforced", "Customer documents remain stored beyond approved retention threshold.", "HIGH", "OPEN", 10),
					buildFinding("Endpoint encryption disabled on two laptops", "Two field devices reported noncompliant disk-encryption posture.", "HIGH", "IN_PROGRESS", 4),
					buildFinding("Stale API keys found in CI variables", "Unused service credentials are still configured in the deployment pipeline.", "CRITICAL", "OPEN", 6),
					buildFinding("Joiner-mover-leaver evidence missing", "HR termination checklist evidence was absent for sampled employees.", "MEDIUM", "OPEN", 11),
					buildFinding("Database patch window missed", "Planned PostgreSQL security patch was deferred without documented approval.", "HIGH", "OPEN", 8),
					buildFinding("Open S3 bucket policy warning", "A storage bucket allows broader read access than intended for internal artifacts.", "CRITICAL", "OPEN", 1),
					buildFinding("Incident playbook approval outdated", "The ransomware response playbook is pending annual business approval.", "LOW", "OPEN", 21),
					buildFinding("User access review incomplete for sales apps", "Managers did not complete attestation for all sales platform users.", "MEDIUM", "IN_PROGRESS", 9),
					buildFinding("Unencrypted SMTP relay detected", "Operational alerts were routed through a relay without enforced TLS.", "HIGH", "OPEN", 12),
					buildFinding("Vendor SOC report missing", "Critical payroll processor SOC 2 report has not been collected this year.", "MEDIUM", "OPEN", 18),
					buildFinding("Antivirus signatures outdated on kiosks", "Retail kiosk devices have not received current AV signatures.", "MEDIUM", "OPEN", 15),
					buildFinding("Public IP whitelist not documented", "Production allowlist entries do not map cleanly to approved business owners.", "LOW", "OPEN", 20),
					buildFinding("Monitoring alerts disabled after migration", "Two infrastructure alerts remained muted after cloud cutover.", "HIGH", "OPEN", 13),
					buildFinding("Segregation of duties conflict in ERP", "One finance user can both create and approve supplier payments.", "CRITICAL", "OPEN", 5),
					buildFinding("Password policy exception expired", "Legacy application password-length exception was never retired.", "MEDIUM", "OPEN", 16),
					buildFinding("Missing secure code review evidence", "Sprint release artifacts do not include required peer security review notes.", "MEDIUM", "IN_PROGRESS", 19),
					buildFinding("Dormant VPN accounts exceed threshold", "Dormant remote-access accounts were not disabled after 45 days.", "HIGH", "OPEN", 6),
					buildFinding("CloudTrail retention below standard", "Audit logs for one cloud account are kept for fewer days than policy requires.", "HIGH", "OPEN", 7),
					buildFinding("Unsupported Java runtime in reporting app", "A reporting component is still running on an unsupported JDK build.", "CRITICAL", "OPEN", 4),
					buildFinding("Physical badge reconciliation delayed", "Badge inventory reconciliation was postponed for the regional office.", "LOW", "OPEN", 25),
					buildFinding("Service account owner not assigned", "A legacy integration account has no documented technical owner.", "MEDIUM", "OPEN", 22),
					buildFinding("Production changes missing rollback plan", "Recent infrastructure changes were approved without rollback documentation.", "MEDIUM", "IN_PROGRESS", 17),
					buildFinding("Data classification banner absent", "Internal document repository lacks required classification banner on uploads.", "LOW", "OPEN", 24),
					buildFinding("Weak cipher suite still enabled", "Obsolete TLS cipher suites remain enabled on one partner-facing endpoint.", "HIGH", "OPEN", 3),
					buildFinding("Closed phishing finding awaiting evidence archive", "Remediation is complete but supporting closure evidence is still being filed.", "LOW", "CLOSED", 30)
			);

			for (AuditFinding finding : demoFindings) {
				AuditFinding savedFinding = auditFindingService.createFinding(finding);
				AuditLog auditLog = new AuditLog();
				auditLog.setEntityName("AuditFinding");
				auditLog.setEntityId(savedFinding.getId());
				auditLog.setAction("CREATE");
				auditLog.setOldValue(null);
				auditLog.setNewValue(savedFinding.getTitle());
				auditLog.setPerformedBy("SYSTEM");
				auditLogRepository.save(auditLog);
			}
		};
	}

	private static AuditFinding buildFinding(
			String title,
			String description,
			String severity,
			String status,
			int dueInDays
	) {
		return AuditFinding.builder()
				.title(title)
				.description(description)
				.severity(severity)
				.status(status)
				.dueDate(LocalDate.now().plusDays(dueInDays))
				.build();
	}

}
