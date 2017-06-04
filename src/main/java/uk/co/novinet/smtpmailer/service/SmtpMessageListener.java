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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import uk.co.novinet.smtpmailer.repository.SmtpMessageRepository;

@Service
public class SmtpMessageListener implements MessageListener {
	private static Log log = LogFactory.getLog(SmtpMessageListener.class);

	@Resource
	private SmtpMessageRepository smtpMessageRepository;

	SMTPServer server;

	public SmtpMessageListener() {
		log.info("Starting SmtpMessageListener");
		Collection<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(this);

		this.server = new SMTPServer(listeners);
		this.server.setPort(8025);
		((MessageListenerAdapter) server.getMessageHandlerFactory()).setAuthenticationHandlerFactory(new AuthHandlerFactory());
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

		smtpMessageRepository.save(buildSmtpMessage(from, recipient, out.toByteArray()));
	}

	private SmtpMessage buildSmtpMessage(String fromAddress, String toAddress, byte[] bytes) {
		try {
			MimeMessage mimeMessage = new MimeMessage(getSession(), new ByteArrayInputStream(bytes));
			MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage);
			mimeMessageParser.parse();
			SmtpMessage smtpMessage = new SmtpMessage().withSentDate(mimeMessage.getSentDate()).withToAddress(toAddress)
					.withFromAddress(fromAddress).withSubject(mimeMessageParser.getSubject())
					.withPlainBody(mimeMessageParser.getPlainContent()).withHtmlBody(mimeMessageParser.getHtmlContent())
					.withAttachments(processAttachments(mimeMessageParser.getAttachmentList()));
			return smtpMessage;
		} catch (Exception e) {
			throw new RuntimeException("Cannot build single message", e);
		}
	}

	private Set<Attachment> processAttachments(List<DataSource> attachmentList) throws IOException {
		Set<Attachment> attachments = new HashSet<Attachment>();

		for (DataSource dataSource : attachmentList) {
			attachments.add(
					new Attachment().withFilename(dataSource.getName()).withContentType(dataSource.getContentType())
							.withBase64EncodedBytes(encodeBase64String(toByteArray(dataSource.getInputStream()))));
		}

		return attachments;
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
					log.info("Username=" + username);
					log.info("Password=" + password);
				}
			};
			ret.addPlugin(new PlainAuthenticationHandler(validator));
			ret.addPlugin(new LoginAuthenticationHandler(validator));
			return ret;
		}
	}
}
