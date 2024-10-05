package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class MatronReportEntity extends ReportEntity{

    private String currentSituation; //Situaición actual
    private String fetalHeartbeat; //Latido del feto
    private String painControl; //Control del dolor
    private String recommendations;

}
