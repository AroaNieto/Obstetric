package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import es.obstetrics.obstetric.backend.repository.SubcategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubcategoryService {
    @Autowired
    private SubcategoryRepository subcategoryRepository;

    public SubcategoryService(SubcategoryRepository subcategoryRepository){
        this.subcategoryRepository = subcategoryRepository;
    }

    public List<SubcategoryEntity> findAll(){
        return subcategoryRepository.findAll();
    }

    public SubcategoryEntity findByName(SubcategoryEntity subcategoryEntity){
        //Hay que comprobar si la subcategoria existe en la misma categoria
        List<SubcategoryEntity> s =  subcategoryRepository.findByName(subcategoryEntity.getName());
        for(SubcategoryEntity subcategory : s){
            if(subcategory != null && subcategory.getCategoryEntity().getName().equals(subcategoryEntity.getCategoryEntity().getName())){
                return subcategory;
            }
        }
        return null;
    }

    public void save(SubcategoryEntity subcategoryEntity){
        subcategoryRepository.save(subcategoryEntity);
    }

    public void delete(SubcategoryEntity subcategoryEntity){

        subcategoryRepository.delete(subcategoryEntity);
    }

    public List<SubcategoryEntity> findByCategoryEntityAndState(CategoryEntity categoryName, String stateActive) {
        return subcategoryRepository.findByCategoryEntityAndState(categoryName,stateActive);
    }

    public List<SubcategoryEntity> findByCategoryEntity(CategoryEntity category) {
        return subcategoryRepository.findByCategoryEntity(category);
    }
}
