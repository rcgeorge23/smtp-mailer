package uk.co.novinet.smtpmailer

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Specification

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class SendAndReceiveSmtpEmailSpecification extends Specification {
	
	EmailTestUtils emailTestUtils
	
	def setup() {
		emailTestUtils = new EmailTestUtils("username", "password")
	}
	
	def "Can send and receive email with plain text body"() {
		given:
		emailTestUtils.sendEmail("from@email.address", "to@email.address", "body", "subject")
		
		when:
		RESTClient client = new RESTClient("http://localhost:8080/")
		HttpResponseDecorator response
		
		while (response == null || response.data.numberOfMessages.toInteger() == 0) {
			response = client.get(path: "/", query: [emailAddress: "to@email.address"])
		}
		
		then:
		println(response.data)
		response.data.numberOfMessages.toInteger() == 1
		response.data.messages[0].fromAddress == "from@email.address"
		response.data.messages[0].toAddress == "to@email.address"
		response.data.messages[0].plainBody == "body"
		response.data.messages[0].subject == "subject"
	}
}
