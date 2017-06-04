package uk.co.novinet.smtpmailer.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Attachment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String filename;
	private String base64EncodedBytes;
	private String contentType;
	private int index;
	
	@ManyToOne
	private SmtpMessage smtpMessage;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setBase64EncodedBytes(String base64EncodedBytes) {
		this.base64EncodedBytes = base64EncodedBytes;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFilename() {
		return filename;
	}

	public String getContentType() {
		return contentType;
	}

	public String getBase64EncodedBytes() {
		return base64EncodedBytes;
	}
	
	public Attachment withFilename(String filename) {
		this.filename = filename;
		return this;
	}
	
	public Attachment withContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
	
	public Attachment withBase64EncodedBytes(String base64EncodedBytes) {
		this.base64EncodedBytes = base64EncodedBytes;
		return this;
	}
	
	public Attachment withIndex(int index) {
		this.index = index;
		return this;
	}

	public SmtpMessage getSmtpMessage() {
		return smtpMessage;
	}

	public void setSmtpMessage(SmtpMessage smtpMessage) {
		this.smtpMessage = smtpMessage;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}
