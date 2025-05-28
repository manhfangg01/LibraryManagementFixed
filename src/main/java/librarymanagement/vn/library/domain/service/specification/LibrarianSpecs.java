package librarymanagement.vn.library.domain.service.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import librarymanagement.vn.library.domain.model.Librarian;
import librarymanagement.vn.library.domain.model.Librarian_;

@Service
public class LibrarianSpecs {
    public Specification<Librarian> emailLike(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                return criteriaBuilder.like(root.get(Librarian_.NAME),
                        "%" + name + "%");
            }
            return null;
        };
    }

    public Specification<Librarian> hasEmail(String email) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(email)) {
                return criteriaBuilder.like(root.get(Librarian_.EMAIL),
                        "%" + email + "%");
            }
            return null;
        };
    }

}
