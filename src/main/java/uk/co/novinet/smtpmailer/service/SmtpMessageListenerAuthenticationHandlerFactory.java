package uk.co.novinet.smtpmailer.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginAuthenticationHandler;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandler;
import org.subethamail.smtp.auth.PluginAuthenticationHandler;
import org.subethamail.smtp.auth.UsernamePasswordValidator;

public class SmtpMessageListenerAuthenticationHandlerFactory implements AuthenticationHandlerFactory {
	private static Log LOGGER = LogFactory.getLog(SmtpMessageListenerAuthenticationHandlerFactory.class);
	
	public AuthenticationHandler create() {
		PluginAuthenticationHandler authenticationHandler = new PluginAuthenticationHandler();
		
		UsernamePasswordValidator validator = new UsernamePasswordValidator() {
			public void login(String username, String password) throws LoginFailedException {
				LOGGER.info("Username=" + username);
				LOGGER.info("Password=" + password);
			}
		};
		
		authenticationHandler.addPlugin(new PlainAuthenticationHandler(validator));
		authenticationHandler.addPlugin(new LoginAuthenticationHandler(validator));
		
		return authenticationHandler;
	}
}