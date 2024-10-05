package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String state;
    private int time;

    private String notice; //Manda un correo para quedar constancia de la cita
    private String reminder; //Un día antes de la cita, manda un recordatorio
    private String hasAttended;

    @OneToOne
    private ReportEntity reportEntity;

    @ManyToOne
    private InsuranceEntity insuranceEntity;
    private String insurancePolice;

    @ManyToOne
    private ScheduleEntity scheduleEntity;

    @ManyToOne
    private PatientEntity patientEntity;

    @ManyToOne
    private AppointmentTypeEntity appointmentTypeEntity;

    @OneToMany(mappedBy = "appointmentEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<NotificationEntity> notifications = new ArrayList<>();

    @Override
    public String toString() {
        return date.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppointmentEntity that = (AppointmentEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
