package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Este campo es obligatorio")
    @NotNull(message = "Este campo es obligatorio")
    private String name;

    @NotEmpty(message = "Este campo es obligatorio")
    @NotNull(message = "Este campo es obligatorio")
    private String description;

    private String state;

    private LocalDate date;

   @OneToMany(mappedBy = "categoryEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<SubcategoryEntity> subcategories = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }
}
