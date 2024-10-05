package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class GynecologistReportEntity extends ReportEntity{

    private String gynecologicalExamination; //Exploración ginecólogica
    private String anamnesis; //Anamnesis
    private String cytology; //Citología
    private String ultrasound; //Ecografía
    private String amniocentesis; //Amniocentesis
    private String analytics; //Analítica
    private String gynecologyAssessment; //Valoración ginecológica
    private String riskFactors; //Factores de riesgo
    private String diagnosis; //Diagnostico
    private String treatment; //Tratamiento
}
