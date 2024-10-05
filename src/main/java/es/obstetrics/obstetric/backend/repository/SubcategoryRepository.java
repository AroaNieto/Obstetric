package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoryRepository extends JpaRepository<SubcategoryEntity, Long> {
    List<SubcategoryEntity> findByName(String name);

    List<SubcategoryEntity> findByCategoryEntity(CategoryEntity category);

    List<SubcategoryEntity> findByCategoryEntityAndState(CategoryEntity categoryName, String stateActive);
}
