package uk.co.novinet.smtpmailer.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class SmtpMessage
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String fromAddress;
	private String toAddress;
	private String subject;
	private String htmlBody;
	private String plainBody;
	private Date sentDate;
	
	@ManyToOne
	private SmtpAuthentication smtpAuthentication;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public void setPlainBody(String plainBody) {
		this.plainBody = plainBody;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public String getPlainBody() {
		return plainBody;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public String getSubject() {
		return subject;
	}
	
	public SmtpAuthentication getSmtpAuthentication() {
		return smtpAuthentication;
	}

	public void setSmtpAuthentication(SmtpAuthentication smtpAuthentication) {
		this.smtpAuthentication = smtpAuthentication;
	}
	
	public SmtpMessage withSubject(String subject) {
		this.subject = subject;
		return this;
	}
	
	public SmtpMessage withFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
		return this;
	}
	
	public SmtpMessage withToAddress(String toAddress) {
		this.toAddress = toAddress;
		return this;
	}
	
	public SmtpMessage withSentDate(Date sentDate) {
		this.sentDate = sentDate;
		return this;
	}
	
	public SmtpMessage withHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
		return this;
	}
	
	public SmtpMessage withPlainBody(String plainBody) {
		this.plainBody = plainBody;
		return this;
	}
	
	public SmtpMessage withSmtpAuthentication(SmtpAuthentication smtpAuthentication) {
		this.smtpAuthentication = smtpAuthentication;
		return this;
	}
}