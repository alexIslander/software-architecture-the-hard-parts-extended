package org.kurron.hard.parts.fulfillment.config

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
    fun fulfillmentQueue(): Queue = Queue("fulfillment.events", true)

    @Bean
    fun fulfillmentFailureQueue(): Queue = Queue("fulfillment.failures", true)

    @Bean
    fun paymentBinding(fulfillmentQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(fulfillmentQueue).to(sagaExchange).with(SagaTopics.PAYMENT_PROCESSED)

    @Bean
    fun failureBinding(fulfillmentFailureQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(fulfillmentFailureQueue).to(sagaExchange).with(SagaTopics.FAILURE)
}
