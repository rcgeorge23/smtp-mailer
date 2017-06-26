package uk.co.novinet.smtpmailer.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.novinet.smtpmailer.model.SmtpAuthentication;
import uk.co.novinet.smtpmailer.model.SmtpMessage;

public interface SmtpMessageRepository extends JpaRepository<SmtpMessage, Long> {
	List<SmtpMessage> findBySmtpAuthenticationAndToAddressOrderBySentDateAsc(SmtpAuthentication smtpAuthentication, String toAddress);
	List<SmtpMessage> findBySmtpAuthenticationAndFromAddressOrderBySentDateAsc(SmtpAuthentication smtpAuthentication, String fromAddress);
	List<SmtpMessage> findBySentDateAfter(Date sentDate);
}
