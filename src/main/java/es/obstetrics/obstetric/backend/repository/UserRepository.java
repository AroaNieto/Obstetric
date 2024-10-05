package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByUsername(String username);

    UserEntity findOneByEmail(String email);

    UserEntity findOneByPhone(String phone);

    UserEntity findOneByDni(String dni);

    List<UserEntity> findByRole(String role);

    UserEntity findOneByDniAndRole(String dni, String rolePatient);
}
