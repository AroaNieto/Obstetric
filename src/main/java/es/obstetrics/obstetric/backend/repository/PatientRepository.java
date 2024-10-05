package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    PatientEntity findByDni(String dni);

    PatientEntity findByEmail(String email);

    PatientEntity findByPhone(String phone);

    PatientEntity findByAccessCode(String accessCode);

    PatientEntity findByUsername(String username);

    PatientEntity findByAccessCodeForgotPassword(String accessCodeForgotPassword);

    @Query("SELECT p FROM PatientEntity p WHERE p.email LIKE %:text%")
    Page<PatientEntity> findByEmailContaining(@Param("text") String filtro, PageRequest of);

    @Query("SELECT p FROM PatientEntity p WHERE p.dni LIKE %:text%")
    Page<PatientEntity> findByDniContaining(@Param("text") String filtro, PageRequest of);

    @Query("SELECT p FROM PatientEntity p WHERE p.phone LIKE %:text%")
    Page<PatientEntity> findByPhoneContaining(@Param("text") String filter, PageRequest of);

    @Query("SELECT p FROM PatientEntity p WHERE p.state LIKE %:text%")
    Page<PatientEntity> findByStateContaining(@Param("text") String filter, PageRequest of);

    Page<PatientEntity> findByStateIn(List<String> states, Pageable pageable);

    Page<PatientEntity> findByStateInAndNameContainingOrStateInAndUsernameContaining(List<String> states1, String name, List<String> states2, String username, Pageable pageable);

    @Query("SELECT p FROM PatientEntity p WHERE p.name LIKE %:text% OR p.username LIKE %:text% OR p.lastName LIKE %:text%")
    Page<PatientEntity> findByNameOrUsernameOrLastNameContaining(@Param("text") String filter, PageRequest of);

    PatientEntity findByDniAndRole(String dni, String rolePatient);
}