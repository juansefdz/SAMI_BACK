package com.sami.sami_app.domain.entities;

import com.sami.sami_app.util.enums.AmbulanceType;
import com.sami.sami_app.util.enums.StatusAmbulance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*-
*----------------------------------------------------------------------------------------------------------
* The Ambulance class defines the attributes of an ambulance, such as its unique identifier,
* vehicle plate number, ambulance type, and associations with drivers and emergency medical technicians (EMTs).
* These attributes are mapped to corresponding columns in the database using the Column decorator, 
* with specific sizes and specifications according to requirements.
*----------------------------------------------------------------------------------------------------------
*/

@Entity(name = "ambulance")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ambulance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ambulance")
    private Long idAmbulance;

    @Column(name = "vehicle_plate", nullable = false, length = 10)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "ambulance_type", nullable = false)
    private AmbulanceType ambulanceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusAmbulance status;

    @Column(name = "latitude_ambulance", nullable = false)
    private Double latitudeAmbulance;

    @Column(name = "longitude_ambulance", nullable = false)
    private Double longitudeAmbulance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_driver", referencedColumnName = "id_user")
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_emt", referencedColumnName = "id_user")
    private User emt;
}
