package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.CenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CenterRepository extends JpaRepository<CenterEntity, Long> {
    CenterEntity findOneByCenterName(String name);

    CenterEntity findOneByPhone(String phone);

    CenterEntity findOneByEmail(String phone);

    @Query("SELECT c FROM CenterEntity c JOIN c.sanitaryEntities s WHERE s.dni = :dni")
    List<CenterEntity> findAllBySanitaryEntity(@Param("dni") String dni);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM CenterEntity c JOIN c.sanitaryEntities s " +
            "WHERE c.id = :centerId AND s.id = :sanitaryId")
    boolean existsSanitaryInCenter(@Param("sanitaryId") Long sanitaryId, @Param("centerId") Long centerId);

}
