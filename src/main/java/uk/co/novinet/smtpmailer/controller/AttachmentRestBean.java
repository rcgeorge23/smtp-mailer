package uk.co.novinet.smtpmailer.controller;

public class AttachmentRestBean {

	private Long id;
	private String filename;
	private String base64EncodedBytes;
	private String contentType;
	private int index;
	
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
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}
