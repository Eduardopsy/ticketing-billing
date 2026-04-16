package com.jeduardo.ticketing.billing.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicCobrancaCriada() {
        return TopicBuilder.name("cobranca.criada")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicCobrancaFinalizada() {
        return TopicBuilder.name("cobranca.finalizada")
                .partitions(3)
                .replicas(1)
                .build();
    }
}