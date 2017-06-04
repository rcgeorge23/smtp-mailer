package uk.co.novinet.smtpmailer

import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailTestUtils {
	public static final String HOST = "localhost"
	public static final String PORT = "8025"
	
	String username
	String password
	
	public EmailTestUtils(String username, String password) {
		this.username = username
		this.password = password
	}
	
	public sendEmail(String from, String to, String body, String subject) {
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
		message.setFrom(new InternetAddress(from))
	 
		InternetAddress toAddress = new InternetAddress(to)
	 
		message.addRecipient(Message.RecipientType.TO, toAddress)
	 
		message.setSubject(subject)
		message.setText(body)
	 
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
