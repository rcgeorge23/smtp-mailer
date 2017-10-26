package uk.co.novinet.smtpmailer.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class Attachment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@NotNull
	private String filename;
	@NotNull
	private String base64EncodedBytes;
	@NotNull
	private String contentType;
	@NotNull
	private int idx;
	
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
	
	public Attachment withIndex(int idx) {
		this.idx = idx;
		return this;
	}
	
	public Attachment withSmtpMessage(SmtpMessage smtpMessage) {
		this.smtpMessage = smtpMessage;
		return this;
	}

	public SmtpMessage getSmtpMessage() {
		return smtpMessage;
	}

	public void setSmtpMessage(SmtpMessage smtpMessage) {
		this.smtpMessage = smtpMessage;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}
	
}
