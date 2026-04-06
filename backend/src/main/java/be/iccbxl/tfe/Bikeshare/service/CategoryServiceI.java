package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.model.Category;
import java.util.List;

public interface CategoryServiceI {
    List<Category> getAllCategory();
    Category getCategoryById(Long id);
    Category saveCategory(Category cat);
    Category updateCategory(Long id, Category cat);
    void deleteCategory(Long id);
}
