package uk.co.novinet.smtpmailer

import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailTestUtils {
	public static final String HOST = "localhost"
	public static final String PORT = "8025"
	
	String username
	String password
	
	public EmailTestUtils(String username, String password) {
		this.username = username
		this.password = password
	}
	
	public sendEmail(String from, String to, String body, String subject, Map<String, String> filesToAttach = [:] as Map) {
		String host = "localhost"
		Properties props = System.getProperties()
//		props.put("mail.smtp.starttls.enable",true)
		/* mail.smtp.ssl.trust is needed in script to avoid error "Could not convert socket to TLS"  */
		props.setProperty("mail.smtp.ssl.trust", HOST)
		props.put("mail.smtp.auth", true)
		props.put("mail.smtp.host", HOST)
		props.put("mail.smtp.user", username)
		props.put("mail.smtp.password", password)
		props.put("mail.smtp.port", PORT)
	 
		Session session = Session.getDefaultInstance(props, null)
		MimeMessage message = new MimeMessage(session)
		Multipart multipart = new MimeMultipart()
		
		message.setFrom(new InternetAddress(from))
		message.setSubject(subject)
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))

		MimeBodyPart messageBodyPart = new MimeBodyPart()
		messageBodyPart.setContent(body, "text/plain")
		multipart.addBodyPart(messageBodyPart)
		
		filesToAttach.each { filename, fileContent ->
			File tempFile = File.createTempFile(filename, "smtpMailerIntegrationTestTempFile")
			tempFile.deleteOnExit()
			tempFile.setText(fileContent)
			MimeBodyPart attachmentPart = new MimeBodyPart()
			DataSource source = new FileDataSource(tempFile.getPath())
			attachmentPart.setDataHandler(new DataHandler(source))
			attachmentPart.setFileName(filename)
			multipart.addBodyPart(attachmentPart)
		}
		
		message.setContent(multipart)
	 
		Transport transport 
		
		try {
			transport = session.getTransport("smtp")
			transport.connect(host, username, password)
			transport.sendMessage(message, message.getAllRecipients())
		} finally {
			transport?.close()
		}
	}
}
