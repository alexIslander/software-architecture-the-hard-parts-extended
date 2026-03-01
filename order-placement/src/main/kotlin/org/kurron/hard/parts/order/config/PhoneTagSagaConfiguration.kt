package org.kurron.hard.parts.order.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(PhoneTagSagaProperties::class)
class PhoneTagSagaConfiguration {

    @Bean
    fun phoneTagRestClient(builder: RestClient.Builder): RestClient = builder.build()
}
