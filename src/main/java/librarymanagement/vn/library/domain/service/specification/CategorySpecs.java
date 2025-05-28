package librarymanagement.vn.library.domain.service.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import librarymanagement.vn.library.domain.model.Category;
import librarymanagement.vn.library.domain.model.Category_;

@Service
public class CategorySpecs {
    public Specification<Category> nameLike(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                return criteriaBuilder.like(root.get(Category_.NAME),
                        "%" + name + "%");
            }
            return null;
        };
    }

    public Specification<Category> hasDescription(String description) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(description)) {
                return criteriaBuilder.like(root.get(Category_.DESCRIPTION),
                        "%" + description + "%");
            }
            return null;
        };

    }

}
