package uk.co.novinet.smtpmailer.service;

public class Status {

	private boolean running;
	private int port;
	private String startedDate;
	private long totalNumberOfMessages;
	private long numberOfUniqueEmailAddresses;
	private long maxCacheSize;

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getStartedDate() {
		return startedDate;
	}

	public void setStartedDate(String startedDate) {
		this.startedDate = startedDate;
	}

	public long getTotalNumberOfMessages() {
		return totalNumberOfMessages;
	}

	public void setTotalNumberOfMessages(long totalNumberOfMessages) {
		this.totalNumberOfMessages = totalNumberOfMessages;
	}

	public long getNumberOfUniqueEmailAddresses() {
		return numberOfUniqueEmailAddresses;
	}

	public void setNumberOfUniqueEmailAddresses(long numberOfUniqueEmailAddresses) {
		this.numberOfUniqueEmailAddresses = numberOfUniqueEmailAddresses;
	}

	public long getMaxCacheSize() {
		return maxCacheSize;
	}

	public void setMaxCacheSize(long maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}
}
