package com.socialnetwork.auth.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Конфигурация Producer Factory
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Адрес Kafka брокера
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Сериализатор для ключа (String)
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Сериализатор для значения (JSON)
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Настройки для надёжности
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Ждать подтверждения от всех реплик
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // Количество повторов при ошибке
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Идемпотентность

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate для отправки сообщений
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
