package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //LA clase y sus herencias se mapeará en una sola tabla de la BD
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotEmpty
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres.")
    private String name;

    @NotNull
    @NotEmpty
    @Size(min = 3, max = 50, message = "Los apellidos deben tener entre 3 y 50 caracteres.")
    private String lastName;

    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 50 caracteres.")
    private String username;

    @NotNull
    @NotEmpty
    @Pattern(regexp = "[0-9]{8}[A-Za-z]", message = "Formato incorrecto de DNI") //EJEMPLO: 70836704-Q
    private String dni;

    private String sex;

    @Size(min = 5, max = 100, message = "La dirección debe tener entre 5 y 100 caracteres.")
    private String address;

    @NotEmpty
    @NotNull
    @Size(min = 9, max = 15, message = "Formato incorrecto de teléfono.")
    private String phone;

    @NotNull
    @NotEmpty
    @Email
    private String email;

    private String role;

    @Pattern(regexp = "\\d{5}", message = "Formato incorrecto de código postal.")
    private String postalCode;

    @NotEmpty
    @NotNull
    @Size(min = 2, max = 2, message = "Formato incorrecto de edad.")
    private String age;

    private byte[] profilePhoto;

    private String state;

    private String passwordHash;
    private String stateMessagingSystemPatient;
    private String stateMessagingSystemSanitary;

    @OneToMany(mappedBy = "userEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<NotificationEntity> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MessengerServiceEntity> messagesChat = new ArrayList<>();

    @OneToMany(mappedBy = "userEntity",
            cascade = CascadeType.ALL, //Todas las operaciones de persistencia también se aplicarán en la entidad Content
            orphanRemoval = true,
            fetch = FetchType.EAGER) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas) //SI se elimina esta entidad se eliminará tambien las entidades content asociadas
    private List<LoginLogOutLogEntity> logs = new ArrayList<>();

    @Override
    public String toString() {
        return name + " "+ lastName;
    }
}
