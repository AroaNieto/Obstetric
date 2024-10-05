package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.AppointmentTypeEntity;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByReminderAndState(String responseYes, String stateActive);

    List<AppointmentEntity> findByNoticeAndState(String responseYes, String stateActive);

    List<AppointmentEntity> findByDateAndState(LocalDate now, String stateActive);

    Page<AppointmentEntity> findByAppointmentTypeEntityDescriptionContaining(AppointmentTypeEntity filter, PageRequest of);

    Page<AppointmentEntity> findByDate(LocalDate value, PageRequest of);

    Page<AppointmentEntity> findByPatientEntityNameContainingOrPatientEntityLastNameContaining(String name, String lastName, PageRequest pageable);

    List<AppointmentEntity> findByPatientEntity(PatientEntity patientEntity);

    List<AppointmentEntity> findByPatientEntityAndState(PatientEntity userEntity, String stateActive);

    List<AppointmentEntity> findByDateAndStateAndScheduleEntityDiaryEntitySanitaryEntity(LocalDate now, String stateActive, SanitaryEntity currentUser);

    List<AppointmentEntity> findByStartTimeAndStateAndPatientEntity(LocalTime date, String stateActive, PatientEntity patientEntity);

    List<AppointmentEntity> findByStartTimeAndStateAndScheduleEntityDiaryEntitySanitaryEntity(LocalTime startTime, String stateActive, SanitaryEntity sanitaryEntity);
}
