package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.repository.SanitaryRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SanitaryService {
    private final SanitaryRepository sanitaryRepository;
    private final EntityManager entityManager;

    @Autowired
    public SanitaryService(SanitaryRepository sanitaryRepository,  EntityManager entityManager){
        this.sanitaryRepository = sanitaryRepository;
        this.entityManager = entityManager;
    }

    public List<SanitaryEntity> findAll() {
        return sanitaryRepository.findAll();
    }

    @Transactional
    public SanitaryEntity save(SanitaryEntity sanitaryEntity) {
        return entityManager.merge(sanitaryEntity);
    }

    public void delete(SanitaryEntity sanitaryEntity) {
        sanitaryRepository.delete(sanitaryEntity);
    }

    public Optional<SanitaryEntity> findById(Long id) {
        return sanitaryRepository.findById(id);
    }

    public SanitaryEntity findOneByDni(String dni) {
        return sanitaryRepository.findByDni(dni);
    }

    public SanitaryEntity findOneByEmail(String email) {
        return sanitaryRepository.findByEmail(email);
    }

    public SanitaryEntity findOneByPhone(String phone) {
        return sanitaryRepository.findByPhone(phone);
    }


    public SanitaryEntity findOneByUsername(String username) {
        return sanitaryRepository.findByUsername(username);
    }

    public Page<SanitaryEntity> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sanitaryRepository.findAll(pageable);
    }

    public Page<SanitaryEntity> findByStateContaining(String filter, int offset, int limit) {
        return sanitaryRepository.findByStateContaining(filter, PageRequest.of(offset, limit));
    }

    public Page<SanitaryEntity> findByRoleContaining(String filter, int offset, int limit) {
        return sanitaryRepository.findByRoleContaining(filter, PageRequest.of(offset, limit));
    }

    public Page<SanitaryEntity> findByPhoneContaining(String filter, int offset, int limit) {
        return sanitaryRepository.findByPhoneContaining(filter, PageRequest.of(offset, limit));
    }

    public Page<SanitaryEntity> findByEmailContaining(String filter, int offset, int limit) {
        return sanitaryRepository.findByEmailContaining(filter, PageRequest.of(offset, limit));
    }

    public Page<SanitaryEntity> findByDniContaining(String filter, int offset, int limit) {
        return sanitaryRepository.findByDniContaining(filter, PageRequest.of(offset, limit));
    }

    public SanitaryEntity findByDni(String dni) {
        return sanitaryRepository.findByDni(dni);
    }

    public SanitaryEntity findByEmail(String email) {
        return sanitaryRepository.findByEmail(email);
    }

    public SanitaryEntity findByPhone(String phone) {
        return sanitaryRepository.findByPhone(phone);
    }

    public List<SanitaryEntity> findByRole(String role) {
        return sanitaryRepository.findByRole(role);
    }

    public Page<SanitaryEntity> findByNameContaining(String filter, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return sanitaryRepository.findByNameContaining(filter,pageable);
    }

    public Page<SanitaryEntity> findByRoleIn(List<String> strings, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return sanitaryRepository.findByRoleIn(strings,pageable);
    }

    public long countByRoles(List<String> roles) {
        return sanitaryRepository.countByRoleIn(roles);
    }

    public Page<SanitaryEntity>  findByNameContainingAndRoleIn(String filter, List<String> list, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return sanitaryRepository.findByNameContainingAndRoleIn(filter,list,pageable);
    }

    public long countByNameContainingAndRoles(String filter, List<String> list) {
        return sanitaryRepository.countByNameContainingAndRoleIn(filter,list);
    }

    public Page<SanitaryEntity> findByStateContainingAndRoleIn(String filter, List<String> list, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return sanitaryRepository.findByStateContainingAndRoleIn(filter,list,pageable);
    }

    public Page<SanitaryEntity> findByPhoneContainingAndRoleIn(String filter, List<String> list, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return sanitaryRepository.findByPhoneContainingAndRoleIn(filter,list,pageable);
    }

    public Page<SanitaryEntity> findByEmailContainingAndRoleIn(String filter, List<String> list, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return sanitaryRepository.findByEmailContainingAndRoleIn(filter,list,pageable);
    }

    public Page<SanitaryEntity>  findByDniContainingAndRoleIn(String filter, List<String> list, int i, int limit) {
        Pageable pageable = PageRequest.of(i, limit);
        return sanitaryRepository.findByDniContainingAndRoleIn(filter,list,pageable);
    }

    public List<SanitaryEntity> findByState(String stateActive) {
        return sanitaryRepository.findByState(stateActive);
    }

    public List<SanitaryEntity> findByCenterEntity(CenterEntity value) {
        return sanitaryRepository.findAllByCenterEntity(value.getCenterName());
    }
}