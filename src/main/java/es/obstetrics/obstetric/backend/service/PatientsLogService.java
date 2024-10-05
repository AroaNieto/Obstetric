package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.PatientsLogEntity;
import es.obstetrics.obstetric.backend.repository.PatientsLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientsLogService {
    private final PatientsLogRepository patientsLogRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public PatientsLogService( PatientsLogRepository patientsLogRepository){
        this.patientsLogRepository = patientsLogRepository;
    }
    @Transactional
    public PatientsLogEntity save(PatientsLogEntity patientsLogEntity) {
        return entityManager.merge(patientsLogEntity); //Actualiza o añade una entidad en el contexto de persistencia.
    }

    public Page<PatientsLogEntity> findAll(int i, int limit) {
        return patientsLogRepository.findAll(PageRequest.of(i, limit));
    }

    public Page<PatientsLogEntity> findByPatientEntityNameContaining(String filter, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return patientsLogRepository.findByPatientEntityNameContaining(filter,pageable);
    }

    public List<PatientsLogEntity> findByPatientEntity(PatientEntity patient) {
        return patientsLogRepository.findByPatientEntity(patient);
    }
}
