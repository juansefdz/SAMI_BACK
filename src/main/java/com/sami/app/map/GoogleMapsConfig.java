package com.sami.app.map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.maps.GeoApiContext;
import com.sami.app.common.config.SamiProperties;

/**
 * Solo crea el cliente de Google Maps si hay API key. Sin key la app arranca
 * igual y los tiempos estimados se devuelven vacíos.
 */
@Configuration
public class GoogleMapsConfig {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${sami.google-maps.api-key:}')")
    public GeoApiContext geoApiContext(SamiProperties properties) {
        return new GeoApiContext.Builder()
                .apiKey(properties.googleMaps().apiKey())
                .build();
    }
}
