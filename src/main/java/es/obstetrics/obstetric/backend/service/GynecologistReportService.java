package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.GynecologistReportEntity;
import es.obstetrics.obstetric.backend.repository.GynecologistReportRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GynecologistReportService {
    private final GynecologistReportRepository gynecologistReportRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public GynecologistReportService(GynecologistReportRepository gynecologistReportRepository){
        this.gynecologistReportRepository = gynecologistReportRepository;
    }
    public Page<GynecologistReportEntity> findAll(int page, int size){
        return gynecologistReportRepository.findAll(PageRequest.of(page,size));
    }

    @Transactional
    public GynecologistReportEntity save(GynecologistReportEntity gynecologistReport) {
        return entityManager.merge(gynecologistReport); // Actualiza o añade una entidad en el contexto de persistencia.
    }

    @Transactional
    public void delete(GynecologistReportEntity gynecologistReport){
        gynecologistReportRepository.delete(gynecologistReport);
    }

    public Optional<GynecologistReportEntity> findById(long l) {
        return gynecologistReportRepository.findById(l);
    }
}
