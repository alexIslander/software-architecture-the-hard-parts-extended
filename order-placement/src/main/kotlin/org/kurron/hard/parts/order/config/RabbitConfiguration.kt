package org.kurron.hard.parts.order.config

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
    fun orderQueue(): Queue = Queue("order.events", true)

    @Bean
    fun orderFailureQueue(): Queue = Queue("order.failures", true)

    @Bean
    fun paymentBinding(orderQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(orderQueue).to(sagaExchange).with(SagaTopics.PAYMENT_PROCESSED)

    @Bean
    fun fulfillmentBinding(orderQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(orderQueue).to(sagaExchange).with(SagaTopics.FULFILLMENT_COMPLETED)

    @Bean
    fun emailBinding(orderQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(orderQueue).to(sagaExchange).with(SagaTopics.EMAIL_SENT)

    @Bean
    fun failureBinding(orderFailureQueue: Queue, sagaExchange: TopicExchange) =
        BindingBuilder.bind(orderFailureQueue).to(sagaExchange).with(SagaTopics.FAILURE)
}
