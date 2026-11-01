package com.sami.app.realtime;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.sami.app.account.Account;
import com.sami.app.account.Role;
import com.sami.app.emergency.EmergencyService;
import com.sami.app.security.JwtService;

import lombok.RequiredArgsConstructor;

/**
 * Autentica el CONNECT de STOMP con el header {@code Authorization: Bearer <jwt>}
 * y autoriza cada SUBSCRIBE con las mismas reglas que la API REST.
 */
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final EmergencyService emergencyService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (accessor.getCommand() == StompCommand.CONNECT) {
            accessor.setUser(authenticate(accessor.getFirstNativeHeader("Authorization")));
        } else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeSubscription(accessor);
        } else if (accessor.getCommand() == StompCommand.SEND) {
            throw new MessagingException("Este canal es solo de lectura");
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken authenticate(String header) {
        if (header == null || !header.startsWith(BEARER)) {
            throw new MessagingException("Falta el token");
        }
        String email = jwtService.extractValidSubject(header.substring(BEARER.length()))
                .orElseThrow(() -> new MessagingException("Token inválido o vencido"));
        Account account = (Account) userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth)
                || !(auth.getPrincipal() instanceof Account account)) {
            throw new MessagingException("No autenticado");
        }
        String destination = accessor.getDestination() == null ? "" : accessor.getDestination();

        if (destination.equals(RealtimeNotifier.DISPATCH_TOPIC)) {
            if (account.getRole() != Role.ADMIN) {
                throw new MessagingException("Solo el despacho puede suscribirse");
            }
            return;
        }
        if (destination.startsWith(RealtimeNotifier.EMERGENCY_TOPIC)) {
            try {
                Long id = Long.valueOf(destination.substring(RealtimeNotifier.EMERGENCY_TOPIC.length()));
                emergencyService.getAccessible(id, account); // lanza si no tiene acceso
                return;
            } catch (RuntimeException e) {
                throw new MessagingException("Sin acceso a " + destination);
            }
        }
        throw new MessagingException("Destino no permitido: " + destination);
    }
}
