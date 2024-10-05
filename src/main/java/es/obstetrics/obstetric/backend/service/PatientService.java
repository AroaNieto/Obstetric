package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.repository.PatientRepository;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientEntity> findAll() {
        return patientRepository.findAll();
    }

    public void save(PatientEntity patientEntity) {
        patientRepository.save(patientEntity);
    }

    @Transactional
    public PatientEntity merge(PatientEntity patientEntity) {
        return entityManager.merge(patientEntity);
    }
    public void delete(PatientEntity patientEntity) {
        patientRepository.delete(patientEntity);
    }

    public Optional<PatientEntity> findById(Long id) {
        return patientRepository.findById(id);
    }

    public PatientEntity findOneByDni(String dni) {

        return patientRepository.findByDni(dni);
    }

    public PatientEntity findOneByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    public PatientEntity findOneByPhone(String phone) {
        return patientRepository.findByPhone(phone);
    }

    public PatientEntity findByAccessCodeAndDNI(String accessCode, String dni) {
        PatientEntity userDni = patientRepository.findByDni(dni);
        PatientEntity userAccessCode= patientRepository.findByAccessCode(accessCode);

        if((userDni != null) && (userAccessCode != null) && (userAccessCode.getId().equals(userDni.getId()))){
            if(userAccessCode.getAccessCodeDate().isBefore(LocalDate.now())){ //Si la fecha ha caducado no devuelve nada y resetea el código.
                userAccessCode.setAccessCode("");
                userAccessCode.setAccessCodeDate(null);
                patientRepository.save(userAccessCode);
                return null;
            }
            return userDni;
        }else{
            return null;
        }
    }

    public PatientEntity findOneByUsername(String username) {
        return patientRepository.findByUsername(username);
    }

    public PatientEntity findByAccessCodeForgotPassword(String accessCodeForgotPassword) {
        return patientRepository.findByAccessCodeForgotPassword(accessCodeForgotPassword);
    }

    public Page<PatientEntity> findAll(int page, int size) {
        return patientRepository.findAll(PageRequest.of(page, size));
    }

    public Page<PatientEntity> findByEmailContaining(String filter, int page, int size) {
        return patientRepository.findByEmailContaining(filter, PageRequest.of(page, size));
    }

    public Page<PatientEntity> findByDniContaining(String filter, int page, int size) {
        return patientRepository.findByDniContaining(filter, PageRequest.of(page, size));
    }

    public Page<PatientEntity> findByPhoneContaining(String filter, int page, int size) {
        return patientRepository.findByPhoneContaining(filter, PageRequest.of(page, size));
    }

    public Page<PatientEntity> findByStateContaining(String filter, int page, int size) {
        return patientRepository.findByStateContaining(filter, PageRequest.of(page, size));
    }

    public PatientEntity findByDni(String dni) {
        return patientRepository.findByDni(dni);
    }

    public PatientEntity findByEmail(String email) {
        return  patientRepository.findByEmail(email);
    }

    public PatientEntity findByPhone(String phone) {
        return patientRepository.findByPhone(phone);
    }

    public Page<PatientEntity> findByState(List<String> activeStates, int i, int limit) {
        return patientRepository.findByStateIn(activeStates,PageRequest.of(i, limit));
    }

    public Page<PatientEntity> findByStateInAndNameOrUsernameContaining(List<String> activeStates, String filter, int page, int size) {
        return patientRepository.findByStateInAndNameContainingOrStateInAndUsernameContaining(activeStates, filter, activeStates, filter, PageRequest.of(page, size));
    }

    public Page<PatientEntity> findByNameOrUsernameOrLastNameContaining(String filter, int i, int limit) {
        return patientRepository.findByNameOrUsernameOrLastNameContaining(filter, PageRequest.of(i, limit));
    }

    public PatientEntity findByDniAndRole(String dni) {
        return patientRepository.findByDniAndRole(dni, ConstantUtilities.ROLE_PATIENT);
    }
}
