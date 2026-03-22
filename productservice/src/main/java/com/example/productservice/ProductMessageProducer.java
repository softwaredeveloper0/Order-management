package com.example.productservice;



import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public ProductMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendProductUpdate(ProductMessage message) {
        // Send the message to the exchange with routing key
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                message
        );

        System.out.println("Sent message: " + message);
    }
}
