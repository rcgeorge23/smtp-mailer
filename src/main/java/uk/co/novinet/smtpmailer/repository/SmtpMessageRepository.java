package uk.co.novinet.smtpmailer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.novinet.smtpmailer.model.SmtpMessage;

public interface SmtpMessageRepository extends JpaRepository<SmtpMessage, Long> {
	List<SmtpMessage> findByToAddress(String toAddress);
	List<SmtpMessage> findByFromAddress(String fromAddress);
}
