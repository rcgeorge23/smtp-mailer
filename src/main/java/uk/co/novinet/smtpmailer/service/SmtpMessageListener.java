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
	private static Log LOGGER = LogFactory.getLog(SmtpMessageListener.class);

	@Resource
	private SmtpMessageRepository smtpMessageRepository;
	
	@Resource
	private AttachmentRepository attachmentRepository;
	
	@Resource
	private SmtpAuthenticationRepository smtpAuthenticationRepository;

	SMTPServer server;

	public SmtpMessageListener() {
		LOGGER.info("Starting SmtpMessageListener");
		Collection<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(this);

		this.server = new SMTPServer(listeners);
		this.server.setPort(8025);
		
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
		persistSmtpMessage(getOrCreateSmtpAuthentication(getSession().getProperty("mail.smtp.user"), getSession().getProperty("mail.smtp.password")), from, recipient, out.toByteArray());
	}

	private SmtpAuthentication getOrCreateSmtpAuthentication(String username, String password) {
		List<SmtpAuthentication> smtpAuthentiations = smtpAuthenticationRepository.findByUsernameAndPassword(username, password);
		
		if (smtpAuthentiations.size() > 1) {
			throw new RuntimeException(format("Found more than one smtpAuthentication with username=%s and password=%s", username, password));
		}
		
		if (smtpAuthentiations.size() == 1) {
			return smtpAuthentiations.get(0);
		}
		
		return smtpAuthenticationRepository.save(new SmtpAuthentication()
			.withUsername(username)
			.withPassword(password)
		);
	}

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
			
			smtpMessageRepository.save(smtpMessage);
			persistAttachments(smtpMessage, mimeMessageParser.getAttachmentList());
		} catch (Exception e) {
			throw new RuntimeException("Cannot build single message", e);
		}
	}

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
