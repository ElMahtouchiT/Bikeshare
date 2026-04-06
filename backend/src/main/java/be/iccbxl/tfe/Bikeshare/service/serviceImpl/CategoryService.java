package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Category;
import be.iccbxl.tfe.Bikeshare.repository.CategoryRepository;
import be.iccbxl.tfe.Bikeshare.service.CategoryServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService implements CategoryServiceI {

    @Autowired private CategoryRepository categoryRepository;

    @Override public List<Category> getAllCategory() { return categoryRepository.findAll(); }
    @Override public Category getCategoryById(Long id) { return categoryRepository.findById(id).orElse(null); }
    @Override public Category saveCategory(Category cat) { return categoryRepository.save(cat); }

    @Override
    public Category updateCategory(Long id, Category cat) {
        cat.setId(id);
        return categoryRepository.save(cat);
    }

    @Override public void deleteCategory(Long id) { categoryRepository.deleteById(id); }
}
