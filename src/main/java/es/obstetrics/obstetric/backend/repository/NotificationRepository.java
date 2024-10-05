package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.NotificationEntity;
import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByChanelAndState(String chanel, String messageState);

    @Query("SELECT p FROM NotificationEntity p WHERE p.state LIKE %:text%")
    Page<NotificationEntity> findByStateContaining(@Param("text") String filter, PageRequest of);

    @Query("SELECT p FROM NotificationEntity p WHERE p.chanel LIKE %:text%")
    Page<NotificationEntity> findByChanelContaining(@Param("text") String filter, PageRequest of);

    boolean existsByNewsletterEntityAndUserEntityAndChanel(NewsletterEntity newsletterEntity, UserEntity userEntity, String chanel);

    List<NotificationEntity> findByUserEntity(UserEntity user);

    List<NotificationEntity> findByChanelAndStateAndUserEntity(String messageChanelApp, String messageNotDelivered, UserEntity currentUser);

    boolean existsByAppointmentEntity(AppointmentEntity appointmentEntity);

    Page<NotificationEntity> findByUserEntityNameOrUserEntityLastNameContaining(String filter, String lastname, PageRequest of);
}

