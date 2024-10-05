package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class PregnanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private PatientEntity patientEntity;

    private LocalDate registerDate;

    private LocalDate lastPeriodDate;

    private String state;
    private LocalDate endingDate;
}
