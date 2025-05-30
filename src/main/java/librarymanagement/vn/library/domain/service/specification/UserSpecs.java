package librarymanagement.vn.library.domain.service.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import librarymanagement.vn.library.domain.model.auth.Role;
import librarymanagement.vn.library.domain.model.auth.User;
import librarymanagement.vn.library.domain.model.auth.User_;

@Service
public class UserSpecs {
    public Specification<User> hasName(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                return criteriaBuilder.like(root.get(User_.USERNAME), "%" + name + "%");
            }
            return null;
        };
    }

    public Specification<User> hasAge(int age) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (age > 0) {
                return criteriaBuilder.equal(root.get(User_.AGE), age);
            }
            return null;
        };
    }

    public Specification<User> hasAddress(String address) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(address)) {
                return criteriaBuilder.equal(root.get(User_.ADDRESS), "%" + address + "%");
            }
            return null;
        };
    }

    public Specification<User> hasEmail(String email) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(email)) {
                return criteriaBuilder.like(root.get(User_.EMAIL), "%" + email + "%");
            }
            return null;
        };
    }

    public Specification<User> hasRole(Role role) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (role != null) {
                return criteriaBuilder.equal(root.get(User_.ROLE), role);
            }
            return null;
        };
    }

}
