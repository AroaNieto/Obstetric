package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class PatientEntity extends UserEntity {

    //Código de acceso para registro de la aplicación
    private String accessCode;
    private LocalDate accessCodeDate;

    //Código de acceso para cambiar la contraseña
    private String accessCodeForgotPassword;
    private LocalDate accessCodeForgotPasswordDate;
    private String accessCodeUrlForgotPassword;
    private String chanel; //Canal por el que se enviarán los mensajes

    //Datos médicos
    private String familyBackground; //Antecedentes familiares
    private String personalHistory; //Antecedentes personales
    private String allergies;
    private Integer menarche;
    private LocalDate fur; //Fecha de ultima regla
    private String fm; //Fecha primera menstruación
    private Integer numberOfPregnancies;
    private Integer numberOfAbortions;

    private String bloodType; //Tipo de sangre
    private String rh;
    private byte[] informedConsent;

    @OneToMany(mappedBy = "patientEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<AppointmentEntity> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patientEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<PregnanceEntity> pregnancies = new ArrayList<>();

    @OneToMany(mappedBy = "patientEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<PatientsLogEntity> patientsLogEntities = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "patient_newsletter",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "newsletter_id")
    )
    private List<NewsletterEntity> newsletters = new ArrayList<>();

    @Override
    public String toString() {
        return getName() + " "+ getLastName();
    }
}
