package org.kurron.hard.parts.email.config

import org.kurron.hard.parts.shared.SagaTopics
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfiguration {

    @Bean
    fun sagaExchange(): TopicExchange = TopicExchange(SagaTopics.EXCHANGE, true, false)

    @Bean
    fun emailQueue(): Queue = Queue("email.events", true)

    @Bean
    fun fulfillmentBinding(emailQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(emailQueue).to(sagaExchange).with(SagaTopics.FULFILLMENT_COMPLETED)
}
