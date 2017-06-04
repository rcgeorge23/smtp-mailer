package uk.co.novinet.smtpmailer.service;

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
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.auth.LoginAuthenticationHandler;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandler;
import org.subethamail.smtp.auth.PluginAuthenticationHandler;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.server.MessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import uk.co.novinet.smtpmailer.model.Attachment;
import uk.co.novinet.smtpmailer.model.SmtpMessage;
import uk.co.novinet.smtpmailer.repository.AttachmentRepository;
import uk.co.novinet.smtpmailer.repository.SmtpMessageRepository;

@Service
public class SmtpMessageListener implements MessageListener {
	private static Log LOGGER = LogFactory.getLog(SmtpMessageListener.class);

	@Resource
	private SmtpMessageRepository smtpMessageRepository;
	
	@Resource
	private AttachmentRepository attachmentRepository;

	SMTPServer server;

	public SmtpMessageListener() {
		LOGGER.info("Starting SmtpMessageListener");
		Collection<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(this);

		this.server = new SMTPServer(listeners);
		this.server.setPort(8025);
		((MessageListenerAdapter) server.getMessageHandlerFactory())
				.setAuthenticationHandlerFactory(new AuthHandlerFactory());
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
	public boolean accept(String from, String recipient) {
		return true;
	}

	@Override
	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		data = new BufferedInputStream(data);

		int current;
		while ((current = data.read()) >= 0) {
			out.write(current);
		}

		persistSmtpMessage(from, recipient, out.toByteArray());
		
	}

	private void persistSmtpMessage(String fromAddress, String toAddress, byte[] bytes) {
		try {
			MimeMessage mimeMessage = new MimeMessage(getSession(), new ByteArrayInputStream(bytes));
			MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage);
			mimeMessageParser.parse();
			SmtpMessage smtpMessage = new SmtpMessage()
					.withSentDate(mimeMessage.getSentDate())
					.withToAddress(toAddress)
					.withFromAddress(fromAddress).withSubject(mimeMessageParser.getSubject())
					.withPlainBody(mimeMessageParser.getPlainContent()).withHtmlBody(mimeMessageParser.getHtmlContent());
			
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
					.withBase64EncodedBytes(encodeBase64String(toByteArray(dataSource.getInputStream()))));
		}
	}

	protected Session getSession() {
		return Session.getDefaultInstance(new Properties());
	}

	public SMTPServer getServer() {
		return this.server;
	}

	public class AuthHandlerFactory implements AuthenticationHandlerFactory {
		public AuthenticationHandler create() {
			PluginAuthenticationHandler ret = new PluginAuthenticationHandler();
			UsernamePasswordValidator validator = new UsernamePasswordValidator() {
				public void login(String username, String password) throws LoginFailedException {
					LOGGER.info("Username=" + username);
					LOGGER.info("Password=" + password);
				}
			};
			ret.addPlugin(new PlainAuthenticationHandler(validator));
			ret.addPlugin(new LoginAuthenticationHandler(validator));
			return ret;
		}
	}
}
