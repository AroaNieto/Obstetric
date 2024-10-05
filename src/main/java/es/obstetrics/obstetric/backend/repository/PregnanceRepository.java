package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.PregnanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PregnanceRepository extends JpaRepository<PregnanceEntity, Long> {
    List<PregnanceEntity> findByEndingDateIsNull();

    List<PregnanceEntity> findByPatientEntity(PatientEntity patient);
}
