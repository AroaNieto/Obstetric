package es.obstetrics.obstetric.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
public class MessengerServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private Instant timestamp;
    private Long senderId;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private UserEntity receiver;
    private Instant readingDate;
}