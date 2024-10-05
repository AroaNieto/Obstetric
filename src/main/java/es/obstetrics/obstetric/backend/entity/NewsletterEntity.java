package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class NewsletterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private SubcategoryEntity subcategoryEntity;

    @NotNull
    @NotEmpty
    @Size(min = 5, max =60, message = "Debe contenter entre 5 y 40 caracteres.")
    private String name;

    @NotNull
    @NotEmpty
    @Size(min = 9, max = 100, message = "Debe contenter entre 9 y 27 caracteres.")
    private String summary;

    private String url;

    private byte[] contentBytePdf;
    private byte[] contentByteUrl;
    private byte[] contentMiniature;
    private String state;

    @NotNull
    @NotEmpty
    private String typeContent;

    private LocalDate startDate;
    private LocalDate endingDate;
    private LocalDate date;

    private String quarter;

    @NotNull
    @NotEmpty
    private String  duration;

    @OneToMany(mappedBy = "newsletterEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<NotificationEntity> notifications = new ArrayList<>();

    @ManyToMany(mappedBy = "newsletters", fetch = FetchType.EAGER)
    private List<PatientEntity> patients = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }
}
