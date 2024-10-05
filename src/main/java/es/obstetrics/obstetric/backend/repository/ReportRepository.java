package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    ReportEntity findByAppointmentEntity(AppointmentEntity appointmentEntity);
    List<ReportEntity> findByAppointmentEntityPatientEntityAndState(PatientEntity patientEntity, String stateInactive);
}
