package uk.co.novinet.smtpmailer.service;

public class Email {

	private final static String LABEL = "%s [%d]";

	private String emailAddress;
	private int numberOfEmails;

	/**
	 * @param emailAddress
	 * @param numberOfEmails
	 */
	public Email(String emailAddress, int numberOfEmails) {
		this.emailAddress = emailAddress;
		this.numberOfEmails = numberOfEmails;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public int getNumberOfEmails() {
		return numberOfEmails;
	}

	public String getLabel() {
		return String.format(LABEL, emailAddress, numberOfEmails);
	}
}
