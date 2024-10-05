package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    CategoryEntity findByName(String name);

    CategoryEntity findOneByNameAndState(String label, String stateActive);

    List<CategoryEntity> findByState(String state);
}

