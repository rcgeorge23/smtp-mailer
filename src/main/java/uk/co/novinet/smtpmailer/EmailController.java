package uk.co.novinet.smtpmailer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.subethamail.wiser.WiserMessage;

import uk.co.novinet.smtpmailer.service.FakeSmtpServer;

@RestController
public class EmailController {
	
	private static final String BASE64_BYTES_KEY = "base64Bytes";
	private static final String CONTENT_TYPE_KEY = "contentType";
	private static final String FILENAME_KEY = "filename";
	private static final String ATTACHMENTS_KEY = "attachments";
	private static final String MESSAGES_KEY = "messages";
	private static final String NUMBER_OF_MESSAGES_KEY = "numberOfMessages";
	private static final String BODY_KEY = "Body";
	private static final String PLAIN_KEY = "plain";
	private static final String HTML_KEY = "html";
	private static final String SUBJECT_KEY = "Subject";
	private static final String FROM_KEY = "From";
	private static final String TO_KEY = "To";
	private static final String DATE_KEY = "Date";
	
	@Resource
	protected FakeSmtpServer fakeSmtpServer;

    @RequestMapping("/")
    public Map<String, Object> index(@RequestParam("emailAddress") String emailAddress) {
		List<WiserMessage> smtpMessagesForEmailAddress = new ArrayList<WiserMessage>(fakeSmtpServer.getMessages(emailAddress));

		Collections.reverse(smtpMessagesForEmailAddress);

		Map<String, Object> containerMap = new HashMap<String, Object>();
		List<Map<String, Object>> messageList = new ArrayList<Map<String, Object>>();

		for (WiserMessage mailMessage : smtpMessagesForEmailAddress) {
			messageList.add(buildSingleMessageMap(mailMessage));
		}

		containerMap.put(NUMBER_OF_MESSAGES_KEY, String.valueOf(smtpMessagesForEmailAddress.size()));
		containerMap.put(MESSAGES_KEY, messageList);

		return containerMap;
    }
    
	private Map<String, Object> buildSingleMessageMap(WiserMessage mailMessage) {
		try {
			Map<String, Object> singleMessageMap = new LinkedHashMap<String, Object>();
			MimeMessage mimeMessage = mailMessage.getMimeMessage();
			MimeMessageParser mimeMessageParser = messageParser(mimeMessage);
			mimeMessageParser.parse();
			singleMessageMap.put(DATE_KEY, mimeMessage.getSentDate().toString());
			singleMessageMap.put(TO_KEY, mailMessage.getEnvelopeReceiver());
			singleMessageMap.put(FROM_KEY, mailMessage.getEnvelopeSender());
			singleMessageMap.put(SUBJECT_KEY, mimeMessageParser.getSubject());
			singleMessageMap.put(BODY_KEY, processBody(mimeMessageParser));
			return singleMessageMap;
		} catch (Exception e) {
			throw new RuntimeException("Cannot build single message", e);
		}
	}

	private Map<String, Object> processBody(MimeMessageParser mimeMessageParser) throws IOException {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put(HTML_KEY, mimeMessageParser.getHtmlContent());
		body.put(PLAIN_KEY, mimeMessageParser.getPlainContent());
		body.put(ATTACHMENTS_KEY, processAttachments(mimeMessageParser.getAttachmentList()));
		return body;
	}

	private List<Map<String, String>> processAttachments(List<DataSource> attachmentList) throws IOException {
		List<Map<String, String>> attachments = new ArrayList<Map<String, String>>();
		for (DataSource dataSource : attachmentList) {
			Map<String, String> attachment = new HashMap<String, String>();
			attachment.put(FILENAME_KEY, dataSource.getName());
			attachment.put(CONTENT_TYPE_KEY, dataSource.getContentType());
			attachment.put(BASE64_BYTES_KEY, inputStreamToBase64(dataSource.getInputStream()));
			attachments.add(attachment);
		}
		return attachments;
	}

	private String inputStreamToBase64(InputStream inputStream) throws IOException {
		byte[] bytes = IOUtils.toByteArray(inputStream);
		return Base64.encodeBase64String(bytes);
	}

	protected MimeMessageParser messageParser(MimeMessage mimeMessage) {
		return new MimeMessageParser(mimeMessage);
	}

}