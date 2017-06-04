package uk.co.novinet.smtpmailer.controller;
import static java.lang.String.valueOf;
import static org.apache.commons.beanutils.BeanUtils.copyProperties;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.novinet.smtpmailer.model.Attachment;
import uk.co.novinet.smtpmailer.model.SmtpMessage;
import uk.co.novinet.smtpmailer.repository.AttachmentRepository;
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
	AttachmentRepository attachmentRepository; 
	
	@Resource
	SmtpMessageListener smtpMessageListener;
	
    @RequestMapping("/")
    public Map<String, Object> index(@RequestParam("emailAddress") String emailAddress) throws IllegalAccessException, InvocationTargetException {
		List<SmtpMessage> smtpMessages = smtpMessageRepository.findByToAddressOrderBySentDateDesc(emailAddress);
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put(NUMBER_OF_MESSAGES_KEY, valueOf(smtpMessages.size()));
		result.put(MESSAGES_KEY, buildSmtpMessageRestBeans(smtpMessages));

		return result;
    }

	private List<SmtpMessageRestBean> buildSmtpMessageRestBeans(List<SmtpMessage> smtpMessages) throws IllegalAccessException, InvocationTargetException {
		List<SmtpMessageRestBean> smtpMessageRestBeans = new ArrayList<SmtpMessageRestBean>();
		
		for (SmtpMessage smtpMessage : smtpMessages) {
			SmtpMessageRestBean smtpMessageRestBean = new SmtpMessageRestBean();
			copyProperties(smtpMessageRestBean, smtpMessage);
			List<Attachment> attachments = attachmentRepository.findBySmtpMessageOrderByIndexAsc(smtpMessage);
			List<AttachmentRestBean> attachmentRestBeans = new ArrayList<AttachmentRestBean>();
			
			for (Attachment attachment : attachments) {
				AttachmentRestBean attachmentRestBean = new AttachmentRestBean();
				copyProperties(attachmentRestBean, attachment);
				attachmentRestBeans.add(attachmentRestBean);
			}
			
			smtpMessageRestBean.setAttachments(attachmentRestBeans);
			smtpMessageRestBeans.add(smtpMessageRestBean);
		}
		
		return smtpMessageRestBeans;
	}
    
}