package librarymanagement.vn.library.domain.service.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import librarymanagement.vn.library.domain.model.Member;
import librarymanagement.vn.library.domain.model.Member_;
import librarymanagement.vn.library.util.constant.MembershipType;

@Service
public class MemberSpecs {
    public Specification<Member> nameLike(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                return criteriaBuilder.like(root.get(Member_.NAME), "%" + name + "%");
            }
            return null;
        };
    }

    public Specification<Member> hasEmail(String email) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(email)) {
                return criteriaBuilder.like(root.get(Member_.EMAIL), "%" + email + "%");
            }
            return null;
        };
    }

    public Specification<Member> hasPhone(String phone) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(phone)) {
                return criteriaBuilder.equal(root.get(Member_.PHONE), phone);
            }
            return null;
        };

    }

    public Specification<Member> hasMemberType(MembershipType membershipType) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (membershipType != null) {
                return criteriaBuilder.equal(root.get(Member_.membershipType), membershipType);
            }
            return null;
        };
    }

    public Specification<Member> hasStatus(Boolean status) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (status != null) {
                return criteriaBuilder.equal(root.get(Member_.STATUS), status);
            }
            return null;
        };
    }

}
