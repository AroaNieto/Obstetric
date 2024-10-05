package es.obstetrics.obstetric.backend.service;

import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public CategoryEntity findOneByName(String name){
        return categoryRepository.findByName(name);
    }

    public void save(CategoryEntity categoryEntity){
        categoryRepository.save(categoryEntity);
    }

    public void delete(CategoryEntity categoryEntity){
        categoryRepository.delete(categoryEntity);
    }

    public List<CategoryEntity> findAll(){
        return categoryRepository.findAll();
    }


    public CategoryEntity findOneByNameAndState(String label, String stateActive) {
        return categoryRepository.findOneByNameAndState(label,stateActive);
    }

    public List<CategoryEntity> findByState(String stateActive) {
        return categoryRepository.findByState(stateActive);
    }
}
