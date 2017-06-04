package uk.co.novinet.smtpmailer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.novinet.smtpmailer.model.Attachment;
import uk.co.novinet.smtpmailer.model.SmtpMessage;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
	List<SmtpMessage> findBySmtpMessageOrderByIndexAsc(SmtpMessage smtpMessage);
}
