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
public class SubcategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @NotNull
    private String name;

    @NotEmpty
    @NotNull
    private String description;

    private String state;

    private LocalDate date;

    @ManyToOne
    private CategoryEntity categoryEntity;

    @OneToMany(mappedBy = "subcategoryEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<NewsletterEntity> contentEntities = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }

}
