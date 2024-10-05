package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SanitaryRepository extends JpaRepository<SanitaryEntity, Long> {
    SanitaryEntity findByEmail(String email);
    SanitaryEntity findByPhone(String phone);
    SanitaryEntity findByDni(String dni);
    SanitaryEntity findByUsername(String username);
    Page<SanitaryEntity> findByRoleContaining(String filter, PageRequest of);

    Page<SanitaryEntity> findByStateContaining(String filter, PageRequest of);

    Page<SanitaryEntity> findByPhoneContaining(String filter, PageRequest of);

    Page<SanitaryEntity> findByEmailContaining(String filter, PageRequest of);

    Page<SanitaryEntity> findByDniContaining(String filter, PageRequest of);

    List<SanitaryEntity> findByRole(String role);

    Page<SanitaryEntity> findByNameContaining(String filter, Pageable pageable);

    Page<SanitaryEntity> findByRoleIn(List<String> strings, Pageable pageable);

    long countByRoleIn(List<String> roles);

    Page<SanitaryEntity> findByNameContainingAndRoleIn(String filter, List<String> list, Pageable pageable);

    long countByNameContainingAndRoleIn(String filter, List<String> list);

    Page<SanitaryEntity> findByStateContainingAndRoleIn(String filter, List<String> list, Pageable pageable);

    Page<SanitaryEntity> findByPhoneContainingAndRoleIn(String filter, List<String> list, Pageable pageable);

    Page<SanitaryEntity> findByEmailContainingAndRoleIn(String filter, List<String> list, Pageable pageable);

    Page<SanitaryEntity> findByDniContainingAndRoleIn(String filter, List<String> list, Pageable pageable);

    List<SanitaryEntity> findByState(String stateActive);

    @Query("SELECT s FROM SanitaryEntity s JOIN s.centers c WHERE c.centerName = :centerName")
    List<SanitaryEntity> findAllByCenterEntity(@Param("centerName") String centerName);

}
