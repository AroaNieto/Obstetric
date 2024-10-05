package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.GynecologistReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GynecologistReportRepository extends JpaRepository<GynecologistReportEntity, Long> {
}
