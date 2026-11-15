package be.kdg.prog6.flights.adapter;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlightsRabbitTopology {

    public static final String FEEDS_EXCHANGE = "feeds";
    public static final String SSM_ROUTING_KEY = "ssm";
    public static final String SSM_QUEUE = "flights.ssm";
    public static final String DEAD_LETTER_EXCHANGE = "flights.dlx";
    public static final String SSM_DEAD_LETTER_QUEUE = SSM_QUEUE + ".dlq";

    public static final String FLM_ROUTING_KEY = "flm";
    public static final String MVT_ROUTING_KEY_PREFIX = "mvt.";

    @Bean
    DirectExchange flightsFeedsExchange() {
        return new DirectExchange(FEEDS_EXCHANGE);
    }

    @Bean
    Queue ssmQueue() {
        return QueueBuilder.durable(SSM_QUEUE).deadLetterExchange(DEAD_LETTER_EXCHANGE).build();
    }

    @Bean
    Binding ssmBinding() {
        return BindingBuilder.bind(ssmQueue()).to(flightsFeedsExchange()).with(SSM_ROUTING_KEY);
    }

    @Bean
    FanoutExchange flightsDeadLetterExchange() {
        return new FanoutExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    Queue ssmDeadLetterQueue() {
        return QueueBuilder.durable(SSM_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding ssmDeadLetterBinding() {
        return BindingBuilder.bind(ssmDeadLetterQueue()).to(flightsDeadLetterExchange());
    }
}
