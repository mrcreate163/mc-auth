package com.socialnetwork.auth.service;

import com.socialnetwork.auth.dto.kafka.AccountChangedEvent;
import com.socialnetwork.auth.dto.kafka.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String REGISTER_TOPIC = "REGISTER_TOP";
    private static final String ACCOUNT_CHANGES_TOPIC = "ACCOUNT_CHANGES";

    /**
     * Отправка события о регистрации пользователя
     */
    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(REGISTER_TOPIC, event.getUserId().toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send UserRegisteredEvent for userId {}: {}", event.getUserId(), ex.getMessage());
                } else {
                    log.info("Successfully sent UserRegisteredEvent for userId {} to topic-partition {}-{} at offset {}",
                            event.getUserId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Exception while sending UserRegisteredEvent for userId {}: {}", event.getUserId(), e.getMessage());
        }
    }

    /**
     * Отправка события об изменении Email пользователя
     */
    public void sendAccountChangedEvent(AccountChangedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(ACCOUNT_CHANGES_TOPIC, event.getUserId().toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send AccountChangedEvent for userId {}: {}", event.getUserId(), ex.getMessage());
                } else {
                    log.info("Successfully sent AccountChangedEvent for userId {} to topic-partition {}-{} at offset {}",
                            event.getUserId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Exception while sending AccountChangedEvent for userId {}: {}", event.getUserId(), e.getMessage());
        }
    }
}
