package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.LoginLogOutLogEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.repository.LoginLogOutLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginLogOutLogService {
    private final LoginLogOutLogRepository loginLogOutLogRepository;

    @Autowired
    public LoginLogOutLogService(LoginLogOutLogRepository loginLogOutLogRepository){
        this.loginLogOutLogRepository = loginLogOutLogRepository;
    }

    public void save(LoginLogOutLogEntity loginLogOutLogEntity) {
        loginLogOutLogRepository.save(loginLogOutLogEntity); //Actualiza o añade una entidad en el contexto de persistencia.
    }

    public Page<LoginLogOutLogEntity> findByUserEntityNameContaining(String filter, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return loginLogOutLogRepository.findByUserEntityNameContaining(filter,pageable);
    }

    public Page<LoginLogOutLogEntity> findAll(int i, int limit) {
        return loginLogOutLogRepository.findAll(PageRequest.of(i, limit));
    }

    public List<LoginLogOutLogEntity> findByUserEntity(UserEntity user) {
        return loginLogOutLogRepository.findByUserEntity(user);
    }
}
