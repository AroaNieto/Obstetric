package es.obstetrics.obstetric.backend.repository;

import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsletterRepository extends JpaRepository<NewsletterEntity, Long> {

    List<NewsletterEntity> findBySubcategoryEntity(SubcategoryEntity subcategoryEntity);

    List<NewsletterEntity> findBySubcategoryEntityAndState(SubcategoryEntity subcategory, String stateActive);

    List<NewsletterEntity> findByQuarterAndState(String quarter, String stateActive);
}
