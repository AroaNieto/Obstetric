package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.ReportEntity;
import es.obstetrics.obstetric.backend.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;

    public List<ReportEntity> findAll(){
        return reportRepository.findAll();
    }

    public void save(ReportEntity reportEntity){
        reportRepository.save(reportEntity);
    }

    public void delete(ReportEntity reportEntity){
        reportRepository.delete(reportEntity);
    }

    public Optional<ReportEntity> findById(long l) {
        return reportRepository.findById(l);
    }

    public ReportEntity findByAppointmentEntity(AppointmentEntity appointmentEntity) {
        return reportRepository.findByAppointmentEntity(appointmentEntity);
    }

    public List<ReportEntity> findByPatientEntityAndState(PatientEntity patientEntity, String stateInactive) {
        return reportRepository.findByAppointmentEntityPatientEntityAndState(patientEntity,stateInactive);
    }
}
