package uk.co.novinet.smtpmailer.service;

import static java.lang.String.format;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.io.IOUtils.toByteArray;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.MessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import uk.co.novinet.smtpmailer.model.Attachment;
import uk.co.novinet.smtpmailer.model.SmtpAuthentication;
import uk.co.novinet.smtpmailer.model.SmtpMessage;
import uk.co.novinet.smtpmailer.repository.AttachmentRepository;
import uk.co.novinet.smtpmailer.repository.SmtpAuthenticationRepository;
import uk.co.novinet.smtpmailer.repository.SmtpMessageRepository;

@Service
public class SmtpMessageListener implements MessageListener {
	private static final int SMTP_PORT = 8025;

	private static Log LOGGER = LogFactory.getLog(SmtpMessageListener.class);

	@Resource
	private SmtpMessageRepository smtpMessageRepository;
	
	@Resource
	private AttachmentRepository attachmentRepository;
	
	@Resource
	private SmtpAuthenticationRepository smtpAuthenticationRepository;

	SMTPServer server;

	public SmtpMessageListener() {
		LOGGER.info(String.format("Starting SmtpMessageListener on port: %s", SMTP_PORT));
		Collection<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(this);

		this.server = new SMTPServer(listeners);
		this.server.setPort(SMTP_PORT);
		
		MessageListenerAdapter messageHandlerFactory = (MessageListenerAdapter) server.getMessageHandlerFactory();
		messageHandlerFactory.setAuthenticationHandlerFactory(new SmtpMessageListenerAuthenticationHandlerFactory());
		
		start();
	}

	public void setPort(int port) {
		this.server.setPort(port);
	}

	public void setHostname(String hostname) {
		this.server.setHostName(hostname);
	}

	public void start() {
		this.server.start();
	}

	public void stop() {
		this.server.stop();
	}

	@Override
	public boolean accept(String fromAddress, String toAddress) {
		return true;
	}

	@Override
	public void deliver(String fromAddress, String toAddress, InputStream data) throws TooMuchDataException, IOException {
		LOGGER.info(String.format("New message is being delivered. fromAddress: %s, toAddress: %s", fromAddress, toAddress));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		data = new BufferedInputStream(data);

		int current;
		while ((current = data.read()) >= 0) {
			out.write(current);
		}

		persistInTransaction(fromAddress, toAddress, out);
	}

	@Transactional
	private void persistInTransaction(String from, String recipient, ByteArrayOutputStream out) {
		Map<String, String> authenticationMap = SmtpMessageListenerAuthenticationHandlerFactory.AUTHENTICATION_CONTAINER.get();
		persistSmtpMessage(getOrCreateSmtpAuthentication(authenticationMap.get("username"), authenticationMap.get("password")), from, recipient, out.toByteArray());
	}

	@Transactional
	private SmtpAuthentication getOrCreateSmtpAuthentication(String username, String password) {
		LOGGER.info(String.format("getOrCreateSmtpAuthentication called for username: %s and password: %s", username, password));
		
		List<SmtpAuthentication> smtpAuthentiations = smtpAuthenticationRepository.findByUsernameAndPassword(username, password);
		
		LOGGER.info(String.format("Found existing smtpAuthentiations: %s", smtpAuthentiations));
		
		if (smtpAuthentiations.size() > 1) {
			throw new RuntimeException(format("Found more than one smtpAuthentication with username=%s and password=%s", username, password));
		}
		
		if (smtpAuthentiations.size() == 1) {
			return smtpAuthentiations.get(0);
		}
		
		SmtpAuthentication smtpAuthentication = smtpAuthenticationRepository.save(new SmtpAuthentication()
			.withUsername(username)
			.withPassword(password)
		);
		
		LOGGER.info(String.format("Persisted new smtpAuthentiation: %s", smtpAuthentication));
		
		return smtpAuthentication;
	}

	@Transactional
	private void persistSmtpMessage(SmtpAuthentication smtpAuthentication, String fromAddress, String toAddress, byte[] bytes) {
		try {
			MimeMessage mimeMessage = new MimeMessage(getSession(), new ByteArrayInputStream(bytes));
			MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage);
			mimeMessageParser.parse();
			SmtpMessage smtpMessage = new SmtpMessage()
				.withSentDate(mimeMessage.getSentDate())
				.withToAddress(toAddress)
				.withFromAddress(fromAddress).withSubject(mimeMessageParser.getSubject())
				.withPlainBody(mimeMessageParser.getPlainContent()).withHtmlBody(mimeMessageParser.getHtmlContent())
				.withSmtpAuthentication(smtpAuthentication);
			
			LOGGER.info(String.format("Going to persist smtpMessage: %s", smtpMessage));
			
			smtpMessage = smtpMessageRepository.save(smtpMessage);
			
			LOGGER.info(String.format("Persisted smtpMessage: %s", smtpMessage));
			
			persistAttachments(smtpMessage, mimeMessageParser.getAttachmentList());
		} catch (Exception e) {
			throw new RuntimeException("Cannot build single message", e);
		}
	}

	@Transactional
	private void persistAttachments(SmtpMessage smtpMessage, List<DataSource> attachmentList) throws IOException {
		int index = 0;
		for (DataSource dataSource : attachmentList) {
			attachmentRepository.save(new Attachment()
				.withSmtpMessage(smtpMessage)
				.withIndex(index++)
				.withFilename(dataSource.getName())
				.withContentType(dataSource.getContentType())
				.withBase64EncodedBytes(encodeBase64String(toByteArray(dataSource.getInputStream())))
			);
		}
	}

	protected Session getSession() {
		return Session.getDefaultInstance(new Properties());
	}

	public SMTPServer getServer() {
		return this.server;
	}
}
