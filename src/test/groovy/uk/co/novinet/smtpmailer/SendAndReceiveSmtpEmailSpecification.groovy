package uk.co.novinet.smtpmailer

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Specification

//@SpringBootTest(classes = Application.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class SendAndReceiveSmtpEmailSpecification extends Specification {
	
	EmailTestUtils emailTestUtils
	
	def setup() {
		emailTestUtils = new EmailTestUtils("username", "password")
	}
	
	def "hello"() {
		given:
		emailTestUtils.sendEmail("from@email.address", "to@email.address", "body", "subject")
		
		when:
		RESTClient client = new RESTClient("http://localhost:8080/")
		while (true) {
			HttpResponseDecorator response = client.get(path: "/", query: [emailAddress: "to@email.address"])
			println(response.data)
		}
		
		then:
		true
	}
}
