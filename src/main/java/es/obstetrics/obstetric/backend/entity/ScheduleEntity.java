package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class ScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne
    private DiaryEntity diaryEntity;
    private String state;

    @NotNull
    private String maxPatients;

    @OneToMany(mappedBy = "scheduleEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<AppointmentEntity> appointmentEntities = new ArrayList<>();

    @Override
    public String toString() {
        return diaryEntity.getName();
    }
}