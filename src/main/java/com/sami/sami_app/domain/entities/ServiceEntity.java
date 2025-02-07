package com.sami.sami_app.domain.entities;

import java.math.BigDecimal;

import com.sami.sami_app.util.enums.StatusService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    This entity is used to create the service, it has ID, location latitude, location length, 
    the state of the service, a text type description.
    It also has many-to-one references with Hospital, one-to-one with Ambulance, 
    one-to-one with client.... Which allows a relationship between the Service entity and the 
    other entities.
 */

@Entity(name = "service")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Long idService;

    @Column(name = "latitude_service")
    private double latitudeService;

    @Column(name = "longitude_service")
    private double longitudeService;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_service", nullable = false)
    private StatusService status;

    @Lob
    private String anamnesis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_ambulance", referencedColumnName = "id_ambulance")
    private Ambulance ambulance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_hospital", referencedColumnName = "id_hospital")
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_client", referencedColumnName = "id_user")
    private User client;
}
