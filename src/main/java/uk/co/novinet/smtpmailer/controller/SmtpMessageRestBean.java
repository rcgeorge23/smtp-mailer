package uk.co.novinet.smtpmailer.controller;

import java.util.Date;
import java.util.List;

public class SmtpMessageRestBean
{
	private Long id;
	private String fromAddress;
	private String toAddress;
	private String subject;
	private String htmlBody;
	private String plainBody;
	private Date sentDate;
	private List<AttachmentRestBean> attachments;
	
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

	public List<AttachmentRestBean> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentRestBean> attachments) {
		this.attachments = attachments;
	}
}