package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class SanitaryEntity extends UserEntity {

    @OneToMany(mappedBy = "sanitaryEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas)
    private List<DiaryEntity> diaries = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sanitary_center_entity",
            joinColumns = @JoinColumn(name = "sanitary_id"),
            inverseJoinColumns = @JoinColumn(name = "center_id")
    )
    private List<CenterEntity> centers = new ArrayList<>();


    @OneToMany(mappedBy = "sanitaryEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<PatientsLogEntity> patientsLogEntities = new ArrayList<>();

    @Override
    public String toString() {
        return super.getName() + " " + super.getLastName();
    }

}
