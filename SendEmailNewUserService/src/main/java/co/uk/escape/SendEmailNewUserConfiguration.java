package co.uk.escape;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import co.uk.escape.service.ReceiverSendEmailNewRegistrationService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class SendEmailNewUserConfiguration {
	
	final static String queueName = "user-registration";
	
	@Bean
	Queue queue() {
		return new Queue(queueName, false);
	}
	
	@Bean
	RabbitTemplate template(ConnectionFactory connectionFactory){
		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonConverter);
		return rabbitTemplate;
	}
	
//	@Bean
//	FanoutExchange exchange() {
//		return new FanoutExchange("user-registrations-exchange");
//	}

	@Bean
	ReceiverSendEmailNewRegistrationService receiver() {
		return new ReceiverSendEmailNewRegistrationService();
	}
	
	@Bean
	MessageListenerAdapter listenerAdapter(ReceiverSendEmailNewRegistrationService receiver) {
		MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(receiver, "sendEmailNewUser");	
		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
		messageListenerAdapter.setMessageConverter(jsonConverter);
		return messageListenerAdapter;
	}

	
	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {		
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConcurrentConsumers(10);
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(listenerAdapter);
		return container;
	}

}
