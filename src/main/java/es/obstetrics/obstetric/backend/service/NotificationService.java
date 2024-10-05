package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.NotificationEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository){
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationEntity> findAll(){
        return notificationRepository.findAll();
    }


    public void save(NotificationEntity notificationEntity){
        notificationRepository.save(notificationEntity);
    }

    public void delete(NotificationEntity notificationEntity){
        notificationRepository.delete(notificationEntity);
    }

    public Optional<NotificationEntity> findById(Long id) {
        return notificationRepository.findById(id);
    }


    public boolean existsByNewsletterEntityAndUserEntityAndChanel(NotificationEntity notificationEntity) {
        return !notificationRepository.existsByNewsletterEntityAndUserEntityAndChanel(notificationEntity.getNewsletterEntity(),
                notificationEntity.getUserEntity(),
                notificationEntity.getChanel());
    }

    public List<NotificationEntity> findByChanelAndMessageState(String chanel, String messageState) {
        return notificationRepository.findByChanelAndState(chanel, messageState);
    }


    public Page<NotificationEntity> findAll(int page, int size) {
        return notificationRepository.findAll(PageRequest.of(page, size));
    }

    public Page<NotificationEntity> findByMessageStateContaining(String filter, int page, int size) {
        return notificationRepository.findByStateContaining(filter, PageRequest.of(page, size));
    }

    public Page<NotificationEntity> findByChanelContaining(String filter, int page, int size)  {
        return notificationRepository.findByChanelContaining(filter, PageRequest.of(page, size));
    }

    public List<NotificationEntity> findByUserEntity(UserEntity user) {
        return notificationRepository.findByUserEntity(user);
    }

    public List<NotificationEntity> findByChanelAndMessageStateAndUserEntity(String messageChanelApp, String messageNotDelivered, UserEntity currentUser) {
        return notificationRepository.findByChanelAndStateAndUserEntity(messageChanelApp,messageNotDelivered, currentUser);
    }

    public boolean existsByAppointmentEntity(AppointmentEntity appointmentEntity) {
        return notificationRepository.existsByAppointmentEntity(appointmentEntity);
    }

    public Page<NotificationEntity> findByUserEntityNameOrUserEntityLastNameContaining(String filter, String lastname, int i, int limit) {
        return notificationRepository.findByUserEntityNameOrUserEntityLastNameContaining(filter, lastname,PageRequest.of(i, limit));
    }

}
