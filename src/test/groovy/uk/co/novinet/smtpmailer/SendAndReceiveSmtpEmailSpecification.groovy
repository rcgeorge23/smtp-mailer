package uk.co.novinet.smtpmailer

import static org.apache.commons.codec.binary.Base64.encodeBase64String

import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.bind.annotation.RequestParam

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Specification

//@ContextConfiguration(classes = Application.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@ActiveProfiles("test")
class SendAndReceiveSmtpEmailSpecification extends Specification {

	static final String HOST = "localhost"
//	static final String HTTP_PORT = "8080"
//	static final String SMTP_PORT = "8025"
	
//	static final String HOST = "176.9.31.124"
	static final String HTTP_PORT = "80"
	static final String SMTP_PORT = "25"
	
	static final String BASE_URL = "http://${HOST}:${HTTP_PORT}/"
	
	EmailTestUtils emailTestUtils
	
	def setup() {
		emailTestUtils = new EmailTestUtils("username", "password", HOST, SMTP_PORT)
	}
	
	def "Can send and receive email with plain text body"() {
		given:
		emailTestUtils.sendEmail("from@email.address", "to@email.address", "body", "subject")
		
		when:
		RESTClient client = new RESTClient(BASE_URL)
		HttpResponseDecorator response = client.get(path: "/", query: [username: "username", password: "password", toAddress: "to@email.address"])
		
		then:
		println(response.data)
		response.data.numberOfMessages.toInteger() == 1
		response.data.messages[0].fromAddress == "from@email.address"
		response.data.messages[0].toAddress == "to@email.address"
		response.data.messages[0].plainBody == "body"
		response.data.messages[0].subject == "subject"
	}
	
	def "Can send 3 emails and receive them in order they were sent"() {
		given:
		emailTestUtils.sendEmail("from@email.address", "to1@email.address", "body1", "subject1")
		emailTestUtils.sendEmail("from@email.address", "to1@email.address", "body2", "subject2")
		emailTestUtils.sendEmail("from@email.address", "to1@email.address", "body3", "subject3")
		
		when:
		RESTClient client = new RESTClient(BASE_URL)
		HttpResponseDecorator response = client.get(path: "/", query: [username: "username", password: "password", toAddress: "to1@email.address"])
		
		then:
		println(response.data)
		response.data.numberOfMessages.toInteger() == 3
		response.data.messages[0].fromAddress == "from@email.address"
		response.data.messages[0].toAddress == "to1@email.address"
		response.data.messages[0].plainBody == "body1"
		response.data.messages[0].subject == "subject1"
		
		response.data.messages[1].fromAddress == "from@email.address"
		response.data.messages[1].toAddress == "to1@email.address"
		response.data.messages[1].plainBody == "body2"
		response.data.messages[1].subject == "subject2"
		
		response.data.messages[2].fromAddress == "from@email.address"
		response.data.messages[2].toAddress == "to1@email.address"
		response.data.messages[2].plainBody == "body3"
		response.data.messages[2].subject == "subject3"
	}
	
	def "Can send and receive an email with plain text body and 1 attachment"() {
		given:
		emailTestUtils.sendEmail("from@email.address", "to2@email.address", "body", "subject", ["attachment1.txt": "this is attachment 1"])
		
		when:
		RESTClient client = new RESTClient(BASE_URL)
		HttpResponseDecorator response = client.get(path: "/", query: [username: "username", password: "password", toAddress: "to2@email.address"])
		
		then:
		println(response.data)
		response.data.numberOfMessages.toInteger() == 1
		response.data.messages[0].fromAddress == "from@email.address"
		response.data.messages[0].toAddress == "to2@email.address"
		response.data.messages[0].plainBody == "body"
		response.data.messages[0].subject == "subject"
		response.data.messages[0].attachments.size() == 1
		response.data.messages[0].attachments[0].filename == "attachment1.txt"
		response.data.messages[0].attachments[0].base64EncodedBytes == encodeBase64String("this is attachment 1".getBytes())
		response.data.messages[0].attachments[0].contentType == "application/octet-stream"
		response.data.messages[0].attachments[0].index == 0
	}
	
	def "Can send and receive an email with plain text body and 2 attachments"() {
		given:
		emailTestUtils.sendEmail("from@email.address", "to3@email.address", "body", "subject", ["attachment1.txt": "this is attachment 1", "attachment2.txt": "this is attachment 2"])
		
		when:
		RESTClient client = new RESTClient(BASE_URL)
		HttpResponseDecorator response = client.get(path: "/", query: [username: "username", password: "password", toAddress: "to3@email.address"])
		
		then:
		println(response.data)
		response.data.numberOfMessages.toInteger() == 1
		response.data.messages[0].fromAddress == "from@email.address"
		response.data.messages[0].toAddress == "to3@email.address"
		response.data.messages[0].plainBody == "body"
		response.data.messages[0].subject == "subject"
		response.data.messages[0].attachments.size() == 2
		response.data.messages[0].attachments[0].filename == "attachment1.txt"
		response.data.messages[0].attachments[0].base64EncodedBytes == encodeBase64String("this is attachment 1".getBytes())
		response.data.messages[0].attachments[0].contentType == "application/octet-stream"
		response.data.messages[0].attachments[0].index == 0
		response.data.messages[0].attachments[1].filename == "attachment2.txt"
		response.data.messages[0].attachments[1].base64EncodedBytes == encodeBase64String("this is attachment 2".getBytes())
		response.data.messages[0].attachments[1].contentType == "application/octet-stream"
		response.data.messages[0].attachments[1].index == 1
	}
}
