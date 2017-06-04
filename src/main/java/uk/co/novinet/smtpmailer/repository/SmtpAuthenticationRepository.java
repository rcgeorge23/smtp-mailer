package uk.co.novinet.smtpmailer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.novinet.smtpmailer.model.SmtpAuthentication;

public interface SmtpAuthenticationRepository extends JpaRepository<SmtpAuthentication, Long> {
	List<SmtpAuthentication> findByUsernameAndPassword(String username, String password);
}
