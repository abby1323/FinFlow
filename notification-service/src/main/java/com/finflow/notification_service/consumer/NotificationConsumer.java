package com.finflow.notification_service.consumer;

import com.finflow.notification_service.event.NotificationEvent;
import com.finflow.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queues.notification}")
    public void consume(NotificationEvent event){
        log.info("Received notification event for transactionId={}",
                event.getTransactionId());
        notificationService.processNotification(event);
    }
}
