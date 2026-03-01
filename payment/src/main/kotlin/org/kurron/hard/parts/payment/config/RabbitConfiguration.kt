package org.kurron.hard.parts.payment.config

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
    fun paymentQueue(): Queue = Queue("payment.events", true)

    @Bean
    fun paymentFailureQueue(): Queue = Queue("payment.failures", true)

    @Bean
    fun orderPlacedBinding(paymentQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(paymentQueue).to(sagaExchange).with(SagaTopics.ORDER_PLACED)

    @Bean
    fun failureBinding(paymentFailureQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(paymentFailureQueue).to(sagaExchange).with(SagaTopics.FAILURE)
}
