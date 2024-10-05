package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
public class PatientsLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private LocalTime time;

    private String message;
    private String ip; // Nuevo campo para almacenar la IP del servidor

    @ManyToOne
    private PatientEntity patientEntity;

    @ManyToOne
    private SanitaryEntity sanitaryEntity;

}
