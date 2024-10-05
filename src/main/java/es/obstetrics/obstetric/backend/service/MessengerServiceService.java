package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.MessengerServiceEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.repository.MessengerServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessengerServiceService {
    private final MessengerServiceRepository messengerServiceRepository;

    public MessengerServiceService(MessengerServiceRepository messengerServiceRepository) {
        this.messengerServiceRepository = messengerServiceRepository;
    }
    public void save(MessengerServiceEntity message) {
        messengerServiceRepository.save(message);
    }

    public List<MessengerServiceEntity> findByReceiverAndSenderId(UserEntity userReceiver, Long currentUser) {
        return messengerServiceRepository.findByReceiverAndSenderId(userReceiver, currentUser);
    }

    public List<MessengerServiceEntity> findBySenderIdOrReceiverId(Long id, Long id1) {
        return messengerServiceRepository.findBySenderIdOrReceiverId(id,id1);
    }
}

