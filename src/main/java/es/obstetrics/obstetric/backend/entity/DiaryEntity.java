package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class DiaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startTime;
    private LocalDate endTime;
    private String name;
    private String state;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    @ManyToOne
    private CenterEntity centerEntity;

    @ManyToOne
    private SanitaryEntity sanitaryEntity;

    @OneToMany(mappedBy = "diaryEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<ScheduleEntity> schedules = new ArrayList<>();

    @Override
    public String toString() {
        if(endTime == null){
            return name +  " " +startTime + " - ";
        }else{
            return name +  " " +startTime +"-" + endTime;
        }

    }
}
