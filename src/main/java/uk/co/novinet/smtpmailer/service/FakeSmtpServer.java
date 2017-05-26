package uk.co.novinet.smtpmailer.service;

import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Component
public class FakeSmtpServer {

	private static final Logger LOGGER = Logger.getLogger(FakeSmtpServer.class);

	private Integer port;
	private Integer maxQueueSize;
	private Boolean enableFakeSmtpServer = false;

	private Wiser server;
	private LoadingCache<String, List<WiserMessage>> messageCache;
	private Date started = null;
	private ScheduledExecutorService scheduledExecutorService;

	public FakeSmtpServer() {
		this(8025, 100, "true");
	}
	
	/**
	 * @param port
	 * @param maxQueueSize
	 * @param enableFakeSmtpServer
	 */
	public FakeSmtpServer(Integer port, Integer maxQueueSize, String enableFakeSmtpServer) {
		this.port = port;
		this.maxQueueSize = maxQueueSize;
		this.enableFakeSmtpServer = Boolean.valueOf(enableFakeSmtpServer);
		init();
	}

	void init() {
		if (enableFakeSmtpServer) {
			LOGGER.info("Fake SMTP server is enabled and running on port: " + port);
			server = new Wiser();
			server.setPort(port);
			server.start();
			started = new Date();
			messageCache = CacheBuilder.newBuilder().maximumSize(maxQueueSize).expireAfterWrite(6, TimeUnit.HOURS)
					.build(new CacheLoader<String, List<WiserMessage>>() {
						@Override
						public List<WiserMessage> load(String arg0) throws Exception {
							return new ArrayList<WiserMessage>();
						}
					});
			try {
				scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
				scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						consumeNewMailMessages();
					}
				}, 0, 10, TimeUnit.SECONDS);
				LOGGER.info(format("Fake SMTP server started on port: %s with maxQueueSize: %s", port, maxQueueSize));
			} catch (Exception e) {
				LOGGER.error(
						format("Can not start Fake SMTP server on port : %swith maxQueueSize: %s", port, maxQueueSize),
						e);
			}
		} else {
			LOGGER.info("Fake SMTP server has not started as it is set to disabled");
		}
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	/**
	 * Clear cache
	 */
	public void clearCache() {
		if (enableFakeSmtpServer) {
			synchronized (messageCache) {
				messageCache.invalidateAll();
			}
		}
	}

	/**
	 * @param emailAddress
	 * @return
	 */
	public List<WiserMessage> getMessages(String emailAddress) {
		if (enableFakeSmtpServer) {
			LOGGER.debug("=== About to enter synchronized block for getMessages");
			synchronized (messageCache) {
				try {
					return messageCache.get(emailAddress);
				} catch (ExecutionException e) {
					LOGGER.error("Can not get messages for " + emailAddress, e);
				}
			}
			LOGGER.debug("=== Finished synchronized block for getMessages");
		}
		return Collections.emptyList();
	}

	public ConcurrentMap<String, List<WiserMessage>> getAllMessages() {
		ConcurrentMap<String, List<WiserMessage>> copy = new ConcurrentHashMap<String, List<WiserMessage>>();
		if (enableFakeSmtpServer) {
			synchronized (messageCache) {
				copy = messageCache.asMap();
			}
		}
		return copy;
	}

	protected void consumeNewMailMessages() {
		try {
			LOGGER.debug("About to check whether there are any new messages");
			List<WiserMessage> mailMessages = server.getMessages();
			LOGGER.debug(format("Found %d messages", mailMessages.size()));
			for (WiserMessage mailMessage : mailMessages) {
				if (mailMessage != null) {
					String from = mailMessage.getEnvelopeSender();
					String to = mailMessage.getEnvelopeReceiver();
					LOGGER.debug("Email - From : " + from + " To: " + to);
					synchronized (messageCache) {
						List<WiserMessage> messagesForEmailAddress = getMessages(to);
						messagesForEmailAddress.add(mailMessage);
						messageCache.put(to, messagesForEmailAddress);
					}
				}
			}
			LOGGER.debug("Clearing messages from server...");
			server.getMessages().clear();
			LOGGER.debug("Messages cleared");
		} catch (Exception e) {
			LOGGER.error("Can not consume messages", e);
		}
	}

	/**
	 * Restart and clear emails
	 */
	public void restartAndClearEmails() {
		clearCache();
		restart();
	}

	/**
	 * Restart
	 */
	public void restart() {
		LOGGER.info("Stopping fake smtp server");
		if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
			scheduledExecutorService.shutdownNow();
		}
		server.stop();
		init();
	}

	/**
	 * Get status
	 * 
	 * @return
	 */
	public Status status() {
		Status status = new Status();
		status.setRunning(server.getServer().isRunning());
		status.setPort(port);
		status.setStartedDate(getStarted());
		status.setMaxCacheSize(maxQueueSize);
		status.setTotalNumberOfMessages(getNumberOfMessages());
		status.setNumberOfUniqueEmailAddresses(messageCache.size());
		return status;
	}

	private long getNumberOfMessages() {
		int size = 0;
		ConcurrentMap<String, List<WiserMessage>> messages = messageCache.asMap();
		for (String key : messages.keySet()) {
			size += messages.get(key).size();
		}
		return size;
	}

	private String getStarted() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss");
		return sdf.format(started);
	}
}
