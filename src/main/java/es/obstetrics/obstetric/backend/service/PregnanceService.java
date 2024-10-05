package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.PregnanceEntity;
import es.obstetrics.obstetric.backend.repository.PregnanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PregnanceService {

    @Autowired
    private PregnanceRepository pregnanceRepository;

    public PregnanceService(PregnanceRepository pregnanceRepository){
        this.pregnanceRepository = pregnanceRepository;
    }

    public List<PregnanceEntity> findAll(){
        return pregnanceRepository.findAll();
    }

    public void save(PregnanceEntity pregnanceEntity){
        pregnanceRepository.save(pregnanceEntity);
    }

    public void delete(PregnanceEntity pregnanceEntity){
        pregnanceRepository.delete(pregnanceEntity);
    }

    public Optional<PregnanceEntity> findById(Long id) {
        return pregnanceRepository.findById(id);
    }

    public List<PregnanceEntity> findActivePregnancies() {
        return pregnanceRepository.findByEndingDateIsNull();
    }

    public List<PregnanceEntity> findByPatientEntity(PatientEntity patient) {
        return pregnanceRepository.findByPatientEntity(patient);
    }
}
