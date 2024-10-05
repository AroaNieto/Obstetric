package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.InsuranceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InsuranceRepository extends JpaRepository<InsuranceEntity, Long> {

    InsuranceEntity findOneByPhone(String value);
    InsuranceEntity findOneByName(String name);

    Page<InsuranceEntity> findByNameContaining(String filter, PageRequest of);

    Page<InsuranceEntity> findByPostalCodeContaining(String filter, PageRequest of);

    Page<InsuranceEntity> findByAddressContaining(String filter, PageRequest of);

    Page<InsuranceEntity> findByEmailContaining(String filter, PageRequest of);

    Page<InsuranceEntity> findByPhoneContaining(String filter, PageRequest of);

    @Query("SELECT i FROM InsuranceEntity i JOIN i.centers c WHERE c.centerName = :centerName")
    List<InsuranceEntity> findAllByCenterName(@Param("centerName") String centerName);
}