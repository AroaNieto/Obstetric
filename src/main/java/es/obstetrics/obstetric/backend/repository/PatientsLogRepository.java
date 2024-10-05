package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.PatientsLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientsLogRepository extends JpaRepository<PatientsLogEntity, Long> {
    Page<PatientsLogEntity> findByPatientEntityNameContaining(String filter, Pageable pageable);

    List<PatientsLogEntity> findByPatientEntity(PatientEntity patient);
}
