package librarymanagement.vn.library.domain.service.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import librarymanagement.vn.library.domain.model.Book;
import librarymanagement.vn.library.domain.model.Book_;
import librarymanagement.vn.library.domain.model.Category;
import librarymanagement.vn.library.domain.model.Category_;

@Service
public class BookSpecs {
    public Specification<Book> titleLike(String title) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(title)) {
                return criteriaBuilder.like(root.get(Book_.TITLE), "%" + title + "%");
            }
            return null;
        };
    }

    public Specification<Book> hasAuthorName(String authorName) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(authorName)) {
                return criteriaBuilder.like(root.get(Book_.AUTHOR_NAME), "%" + authorName + "%");
            }
            return null;
        };
    }

    public Specification<Book> hasCategories(List<Long> categoryIds) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (!CollectionUtils.isEmpty(categoryIds)) {
                // Kích hoạt select DISTINCT
                criteriaQuery.distinct(true);
                // Tạo ra bảng join giữa categories và books
                Join<Book, Category> joinCategoriesBooksTable = root.join(Book_.CATEGORIES);
                return joinCategoriesBooksTable.get(Category_.ID).in(categoryIds);
            }
            return null;
        };
    }

    public static Specification<Book> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId != null) {
                return criteriaBuilder.equal(root.join(Book_.categories).get(Category_.id), categoryId);
            }
            return null;
        };
    }
}
