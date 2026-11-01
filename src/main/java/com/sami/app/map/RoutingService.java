package com.sami.app.map;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.sami.app.map.dto.Coordinates;
import com.sami.app.map.dto.RouteEstimate;

import lombok.extern.slf4j.Slf4j;

/**
 * Cálculo de rutas con Google Directions. Si no hay API key devuelve vacío.
 * Las respuestas se guardan 30 s por par de puntos redondeados a ~100 m, así
 * varios clientes siguiendo la misma emergencia no multiplican las consultas.
 */
@Slf4j
@Service
public class RoutingService {

    private static final Duration TTL = Duration.ofSeconds(30);

    private final GeoApiContext context;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    private record Cached(Optional<RouteEstimate> value, Instant expiresAt) {
    }

    public RoutingService(ObjectProvider<GeoApiContext> contextProvider) {
        this.context = contextProvider.getIfAvailable();
    }

    public Optional<RouteEstimate> estimate(Coordinates from, Coordinates to) {
        if (context == null || from == null || to == null) {
            return Optional.empty();
        }
        String key = round(from) + ">" + round(to);
        Cached hit = cache.get(key);
        if (hit != null && hit.expiresAt().isAfter(Instant.now())) {
            return hit.value();
        }
        Optional<RouteEstimate> value = query(from, to);
        if (cache.size() > 5_000) {
            cache.clear();
        }
        cache.put(key, new Cached(value, Instant.now().plus(TTL)));
        return value;
    }

    private static String round(Coordinates c) {
        return "%.3f,%.3f".formatted(c.latitude(), c.longitude());
    }

    private Optional<RouteEstimate> query(Coordinates from, Coordinates to) {
        try {
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(toLatLng(from))
                    .destination(toLatLng(to))
                    .mode(TravelMode.DRIVING)
                    .await();
            if (result.routes.length == 0 || result.routes[0].legs.length == 0) {
                return Optional.empty();
            }
            DirectionsLeg leg = result.routes[0].legs[0];
            return Optional.of(new RouteEstimate(leg.distance.inMeters, leg.duration.inSeconds,
                    leg.duration.humanReadable));
        } catch (Exception e) {
            log.warn("No se pudo calcular la ruta: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static LatLng toLatLng(Coordinates c) {
        return new LatLng(c.latitude(), c.longitude());
    }
}
