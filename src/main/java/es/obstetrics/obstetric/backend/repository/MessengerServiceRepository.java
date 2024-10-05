package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.MessengerServiceEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessengerServiceRepository extends JpaRepository<MessengerServiceEntity, Long> {
    List<MessengerServiceEntity> findByReceiverAndSenderId(UserEntity userReceiver, Long currentUser);

    List<MessengerServiceEntity> findBySenderIdOrReceiverId(Long id, Long id1);
}