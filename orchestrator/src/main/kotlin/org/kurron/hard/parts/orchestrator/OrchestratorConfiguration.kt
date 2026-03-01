package org.kurron.hard.parts.orchestrator

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(OrchestratorProperties::class)
class OrchestratorConfiguration {

    @Bean
    fun restClient(builder: RestClient.Builder): RestClient {
        return builder.build()
    }
}
