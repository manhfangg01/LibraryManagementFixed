package librarymanagement.vn.library.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import librarymanagement.vn.library.domain.dto.CategoryFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Category;
import librarymanagement.vn.library.domain.service.CategoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String getAllCategories(
            Model model,
            @RequestParam("page") Optional<Integer> optionalPage,
            @RequestParam("size") Optional<Integer> optionalSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @ModelAttribute CategoryFilterCriteriaDTO categoryFilterCriteriaDTO) {
        int page = optionalPage.orElse(1);
        int size = optionalSize.orElse(5);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        Page<Category> pageCategories;

        pageCategories = categoryService.fetchAllCategoriesWithPaginationAndSpecification(categoryFilterCriteriaDTO,
                pageable);
        List<Category> categories = pageCategories.getContent();

        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("sizePerPage", size);
        model.addAttribute("totalPages", pageCategories.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "categories/show";
    }

    @GetMapping("/categories/create")
    public String getCreateCategoryPage(Model model) {
        model.addAttribute("newCategory", new Category());
        return "/categories/create";
    }

    @PostMapping("/categories/create")
    public String postCreateCategory(Model model, @ModelAttribute("newCategory") Category category) {
        if (category != null) {
            if (this.categoryService.fetchCategoryByName(category.getName()).isPresent()) {
                model.addAttribute("ObjectExisted", "Đã tồn tại thể loại này rồi");
                model.addAttribute("newCategory", category); // giữ lại dữ liệu người dùng nhập
                return "/categories/create";
            } else {
                this.categoryService.saveCategory(category);
            }
        }
        return "redirect:/categories";
    }

    // Hiển thị form cập nhật
    @GetMapping("/categories/edit/{id}")
    public String getEditCategoryPage(Model model, @PathVariable("id") long id) {

        Category category = categoryService.fetchCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("category", category);
        model.addAttribute("id", id);
        return "/categories/edit";
    }

    // Xử lý cập nhật
    @PostMapping("/categories/edit")
    public String postEditCategory(@ModelAttribute("category") Category category, Model model) {

        if (this.categoryService.fetchCategoryByName(category.getName()).isPresent()) {
            model.addAttribute("ObjectExisted", "Đã tồn tại thể loại này rồi");
            model.addAttribute("newCategory", category); // giữ lại dữ liệu người dùng nhập
            return "/categories/edit";
        } else {
            Category realCategory = this.categoryService.fetchCategoryById(category.getId()).get();
            realCategory.setName(category.getName());
            realCategory.setDescription(category.getDescription());
            this.categoryService.saveCategory(realCategory);
        }
        return "redirect:/categories";

    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") long id) {
        this.categoryService.delete(id);
        return "redirect:/categories";
    }

    @GetMapping("/categories/detail/{id}")
    public String getCategoryDetail(@PathVariable("id") long id, Model model) {
        Optional<Category> optionalCategory = this.categoryService.fetchCategoryById(id);
        if (optionalCategory.isEmpty()) {
            return "redirect:/categories";
        }
        model.addAttribute("category", optionalCategory.get());
        return "/categories/detail";
    }

}
