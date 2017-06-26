package uk.co.novinet.smtpmailer.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import uk.co.novinet.smtpmailer.repository.SmtpMessageRepository;

@Service
public class SmtpMessageReaper {
	
	@Resource
	SmtpMessageRepository smtpMessageRepository;

	@Scheduled(fixedRate = 10000)
	public void removeOldSmtpMessages() {
		smtpMessageRepository.findBySentDateAfter(Date.from(LocalDateTime.now().minusHours(4).toInstant(ZoneOffset.UTC)));
	}
}
