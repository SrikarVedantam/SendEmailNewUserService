package co.uk.escape;

import java.io.IOException;

import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import com.rabbitmq.client.Channel;
import co.uk.escape.domain.TemporaryQueue;
import co.uk.escape.service.ReceiverSendEmailNewRegistrationService;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class SendEmailNewUserConfiguration {

	final static String queueName = "user-registration";

	// TODO: This could be replaced with an anonymous queue
	@Bean
	TemporaryQueue temporaryQueue(ConnectionFactory connectionFactory) throws IOException {
		Channel channel=connectionFactory.createConnection().createChannel(false); //String name, boolean durable, boolean exclusive, boolean autoDelete)
		return new TemporaryQueue(channel.queueDeclare().getQueue());
	}
	
	@Bean
	Binding binding(TemporaryQueue temporaryQueue, FanoutExchange exchange) {
		return new Binding(temporaryQueue.getName(), DestinationType.QUEUE, exchange.getName(), "", null);
	}

	@Bean
	FanoutExchange fanoutExchange() {
		return new FanoutExchange("user-registrations-fanout-exchange");
	}

//	@Bean
//	Binding binding(Queue queue, DirectExchange exchange) {
//		return BindingBuilder.bind(queue).to(exchange).with("email");
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
	SimpleMessageListenerContainer container(TemporaryQueue temporaryQueue, ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setMessageListener(listenerAdapter);
		container.setQueueNames(temporaryQueue.getName());
		return container;
	}

}
