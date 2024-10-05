package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String chanel;
    private LocalDate shippingDate;

    @ManyToOne
    private UserEntity userEntity;

    @ManyToOne
    private NewsletterEntity newsletterEntity;

    @ManyToOne
    private AppointmentEntity appointmentEntity;

    private String state;

}
