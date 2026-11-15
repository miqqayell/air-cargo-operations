package be.kdg.prog6.loading.adapter;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadingRabbitTopology {

    public static final String FEEDS_EXCHANGE = "feeds";
    public static final String ULD_ROUTING_KEY = "uld";
    public static final String ULD_QUEUE = "loading.uld";
    public static final String DEAD_LETTER_EXCHANGE = "loading.dlx";
    public static final String ULD_DEAD_LETTER_QUEUE = ULD_QUEUE + ".dlq";

    public static final String CPM_ROUTING_KEY = "cpm";

    @Bean
    DirectExchange loadingFeedsExchange() {
        return new DirectExchange(FEEDS_EXCHANGE);
    }

    @Bean
    Queue uldQueue() {
        return QueueBuilder.durable(ULD_QUEUE).deadLetterExchange(DEAD_LETTER_EXCHANGE).build();
    }

    @Bean
    Binding uldBinding() {
        return BindingBuilder.bind(uldQueue()).to(loadingFeedsExchange()).with(ULD_ROUTING_KEY);
    }

    @Bean
    FanoutExchange loadingDeadLetterExchange() {
        return new FanoutExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    Queue uldDeadLetterQueue() {
        return QueueBuilder.durable(ULD_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding uldDeadLetterBinding() {
        return BindingBuilder.bind(uldDeadLetterQueue()).to(loadingDeadLetterExchange());
    }
}
