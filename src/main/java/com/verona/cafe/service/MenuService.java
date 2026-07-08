package com.verona.cafe.service;

import com.verona.cafe.model.Category;
import com.verona.cafe.model.MenuItem;
import com.verona.cafe.repository.CategoryRepository;
import com.verona.cafe.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.ArrayList;

@Service
public class MenuService {
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuService(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Category ID: " + id));
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public List<MenuItem> searchMenuItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMenuItems();
        }
        String normalizedKeyword = keyword.trim();
        return menuItemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedKeyword, normalizedKeyword);
    }

    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Menu Item ID: " + id));
    }

    public MenuItem saveMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    public List<MenuItem> getMenuItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }

    public List<MenuItem> getTopSellingMenuItems(int limit) {
        List<MenuItem> queryResult = menuItemRepository.findTopSellingMenuItems(PageRequest.of(0, limit));
        List<MenuItem> topSellers = new ArrayList<>(queryResult);
        if (topSellers.size() < limit) {
            List<MenuItem> all = menuItemRepository.findAll();
            for (MenuItem item : all) {
                if (topSellers.size() >= limit) break;
                if (topSellers.stream().noneMatch(existing -> existing.getId().equals(item.getId()))) {
                    topSellers.add(item);
                }
            }
        }
        return topSellers;
    }
}
