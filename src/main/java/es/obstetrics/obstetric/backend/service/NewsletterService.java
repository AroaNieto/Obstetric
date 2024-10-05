package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import es.obstetrics.obstetric.backend.repository.NewsletterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NewsletterService {
    @Autowired
    private NewsletterRepository newsletterRepository;

    public NewsletterService(NewsletterRepository newsletterRepository){

        this.newsletterRepository = newsletterRepository;
    }

    public List<NewsletterEntity> findAll(){
        return newsletterRepository.findAll();
    }

    public List<NewsletterEntity> findBySubcategory(SubcategoryEntity subcategoryEntity){
        return newsletterRepository.findBySubcategoryEntity(subcategoryEntity);
    }

    public void save(NewsletterEntity newsletterEntity){
        newsletterRepository.save(newsletterEntity);
    }

    public void delete(NewsletterEntity newsletterEntity){
        newsletterRepository.delete(newsletterEntity);
    }

    public Optional<NewsletterEntity> findById(Long id) {
        return newsletterRepository.findById(id);
    }

    public List<NewsletterEntity> findByQuarterAndState(String quarter, String stateActive) {
        return newsletterRepository.findByQuarterAndState(quarter,stateActive);
    }

    public List<NewsletterEntity> findBySubcategoryEntityAndState(SubcategoryEntity subcategory, String stateActive) {
        return newsletterRepository.findBySubcategoryEntityAndState(subcategory,stateActive);
    }
}
