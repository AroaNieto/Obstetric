package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.AppointmentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentTypeEntity, Long> {
    AppointmentTypeEntity findOneByDescription(String description);
}
