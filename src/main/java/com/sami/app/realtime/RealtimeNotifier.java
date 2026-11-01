package com.sami.app.realtime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

/**
 * Publica los eventos por WebSocket solo después de confirmar la transacción,
 * para que el cliente nunca lea datos que luego se revierten.
 *
 * Tópicos:
 *  - /topic/emergencies/{id}: quien tenga acceso a esa emergencia
 *  - /topic/dispatch: administradores (tablero de despacho)
 */
@Component
@RequiredArgsConstructor
public class RealtimeNotifier {

    public static final String EMERGENCY_TOPIC = "/topic/emergencies/";
    public static final String DISPATCH_TOPIC = "/topic/dispatch";

    private final SimpMessagingTemplate messaging;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(EmergencyEvent event) {
        messaging.convertAndSend(EMERGENCY_TOPIC + event.emergencyId(), event);
        messaging.convertAndSend(DISPATCH_TOPIC, event);
    }
}
