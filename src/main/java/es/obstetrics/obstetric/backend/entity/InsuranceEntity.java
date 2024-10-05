package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
public class InsuranceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotEmpty
    private String name;

    private String phone;

    @Email
    private String email;

    private String address;

    private String postalCode;
    private String state;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "insurance_center_entity",
            joinColumns = @JoinColumn(name = "insurance_id"),
            inverseJoinColumns = @JoinColumn(name = "center_id")
    )
    private List<CenterEntity> centers = new ArrayList<>();

    @OneToMany(mappedBy = "insuranceEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<AppointmentEntity> appointmentEntities = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InsuranceEntity that = (InsuranceEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
