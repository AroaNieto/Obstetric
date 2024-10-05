package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //La clase y sus herencias se mapeará en una sola tabla de la BD
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private byte[] pdf;
    private String state;

    @OneToOne(cascade = CascadeType.ALL)
    private AppointmentEntity appointmentEntity;

    private LocalDate date;
    private String reasonForConsultation; //Motivo de consulta
    private String subjectiveEvaluations; //Evaluaciones subjetivas
    private String observations; //Observaciones
}
