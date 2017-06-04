package uk.co.novinet.smtpmailer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.novinet.smtpmailer.model.SmtpMessage;
import uk.co.novinet.smtpmailer.repository.SmtpMessageRepository;
import uk.co.novinet.smtpmailer.service.SmtpMessageListener;

@RestController
public class EmailController {
	
	private static final String MESSAGES_KEY = "messages";
	private static final String NUMBER_OF_MESSAGES_KEY = "numberOfMessages";
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(EmailController.class);
	
	@Resource
	SmtpMessageRepository smtpMessageRepository; 
	
	@Resource
	SmtpMessageListener smtpMessageListener;
	
    @RequestMapping("/")
    public Map<String, Object> index(@RequestParam("emailAddress") String emailAddress) {
		List<SmtpMessage> smtpMessagesForEmailAddress = smtpMessageRepository.findByToAddress(emailAddress);
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put(NUMBER_OF_MESSAGES_KEY, String.valueOf(smtpMessagesForEmailAddress.size()));
		result.put(MESSAGES_KEY, smtpMessagesForEmailAddress);

		return result;
    }
    
}