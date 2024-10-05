package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.MatronReportEntity;
import es.obstetrics.obstetric.backend.repository.MatronReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class MatronReportService {
    private final MatronReportRepository matronReportRepository;

    @Autowired
    public MatronReportService(MatronReportRepository matronReportRepository){
        this.matronReportRepository = matronReportRepository;
    }
    public Page<MatronReportEntity> findAll(int page, int size){
        return matronReportRepository.findAll(PageRequest.of(page,size));
    }

    public MatronReportEntity save(MatronReportEntity matronReportEntity) {
        return matronReportRepository.save(matronReportEntity);
    }

    public void delete(MatronReportEntity matronReportEntity){
        matronReportRepository.delete(matronReportEntity);
    }

    public Optional<MatronReportEntity> findById(long l) {
        return matronReportRepository.findById(l);
    }
}