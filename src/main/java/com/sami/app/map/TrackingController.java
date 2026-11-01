package com.sami.app.map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sami.app.account.Account;
import com.sami.app.map.dto.TrackingResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Seguimiento")
@RestController
@RequestMapping("/emergencies/{id}/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @Operation(summary = "Posiciones de paciente, ambulancia y hospital con tiempos estimados")
    @GetMapping
    public TrackingResponse track(@PathVariable Long id, @AuthenticationPrincipal Account account) {
        return trackingService.track(id, account);
    }
}
