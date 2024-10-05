package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.LoginLogOutLogEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogOutLogRepository extends JpaRepository<LoginLogOutLogEntity, Long> {
    Page<LoginLogOutLogEntity> findByUserEntityNameContaining(String filter, Pageable pageable);

    List<LoginLogOutLogEntity> findByUserEntity(UserEntity user);
}
