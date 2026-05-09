package com.example.notificationservice.event;

import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleTransferCompleted(TransferCompletedEvent event) {
        log.info("handleTransferCompleted, Received TransferCompletedEvent: {}", event);
        notificationService.sendTransferNotification(
                event.getFromUserId(),
                event.getToUserId(),
                event.getAmount(),
                "SUCCESS"
        );
    }

    @EventListener
    public void handleTransferFailed(TransferFailedEvent event) {
        log.info("handleTransferCompleted, Received TransferFailedEvent: {}", event);
        notificationService.sendTransferNotification(
                event.getFromUserId(),
                event.getToUserId(),
                event.getAmount(),
                "FAILED"
        );
    }

}
