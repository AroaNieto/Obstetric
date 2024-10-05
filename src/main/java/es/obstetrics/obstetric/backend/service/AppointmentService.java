package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.AppointmentTypeEntity;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.repository.AppointmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository){
        this.appointmentRepository = appointmentRepository;
    }
    public List<AppointmentEntity> findAll(){
        return appointmentRepository.findAll();
    }

    @Transactional
    public AppointmentEntity save(AppointmentEntity appointmentEntity) {
        return entityManager.merge(appointmentEntity);
    }

    public void delete(AppointmentEntity appointmentEntity){
        appointmentRepository.delete(appointmentEntity);
    }

    public List<AppointmentEntity> findByReminderAndState(String responseYes, String stateActive) {
        return appointmentRepository.findByReminderAndState(responseYes,stateActive);
    }

    public List<AppointmentEntity> findByNoticeAndState(String responseYes, String stateActive) {
        return appointmentRepository.findByNoticeAndState(responseYes,stateActive);
    }

    public List<AppointmentEntity> findByDateAndState(LocalDate now, String stateActive) {
        return appointmentRepository.findByDateAndState(now,stateActive);
    }

    public Page<AppointmentEntity> findAll(int page, int size) {
        return appointmentRepository.findAll(PageRequest.of(page, size));
    }

    public Page<AppointmentEntity> findByAppointmentTypeDescriptionContaining(AppointmentTypeEntity filter, int offset, int limit) {
        return appointmentRepository.findByAppointmentTypeEntityDescriptionContaining(filter, PageRequest.of(offset, limit));
    }

    public Page<AppointmentEntity> findByDate(LocalDate value, int offset, int limit) {
        return appointmentRepository.findByDate(value, PageRequest.of(offset, limit));
    }

    public Page<AppointmentEntity> findByPatientEntityNameContainingOrPatientEntityLastnameContaining(String filter, int i, int limit) {
        return appointmentRepository.findByPatientEntityNameContainingOrPatientEntityLastNameContaining(filter,filter, PageRequest.of(i, limit));
    }

    public List<AppointmentEntity> findByPatientEntity(PatientEntity patientEntity) {
        return appointmentRepository.findByPatientEntity(patientEntity);
    }

    public Optional<AppointmentEntity> findById(long l) {
        return appointmentRepository.findById(l);
    }

    public List<AppointmentEntity> findByPatientEntityAndState(PatientEntity userEntity, String stateActive) {
        return appointmentRepository.findByPatientEntityAndState(userEntity,stateActive);
    }

    public List<AppointmentEntity> findByDateAndStateAndScheduleEntityDiaryEntitySanitaryEntity(LocalDate now, String stateActive, SanitaryEntity currentUser) {
        return appointmentRepository.findByDateAndStateAndScheduleEntityDiaryEntitySanitaryEntity(now,stateActive,currentUser);
    }

    public List<AppointmentEntity> findByStartTimeAndStateAndPatientEntity(LocalTime date, String stateActive, PatientEntity patientEntity) {
        return appointmentRepository.findByStartTimeAndStateAndPatientEntity(date,stateActive,patientEntity);
    }

    public List<AppointmentEntity> findByStartTimeAndStateAndScheduleEntityDiaryEntitySanitaryEntity(LocalTime startTime, String stateActive, SanitaryEntity sanitaryEntity) {
        return appointmentRepository.findByStartTimeAndStateAndScheduleEntityDiaryEntitySanitaryEntity(startTime,stateActive,sanitaryEntity);
    }
}
