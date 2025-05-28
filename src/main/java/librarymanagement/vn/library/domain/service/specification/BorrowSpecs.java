package librarymanagement.vn.library.domain.service.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import librarymanagement.vn.library.domain.model.Book;
import librarymanagement.vn.library.domain.model.Book_;
import librarymanagement.vn.library.domain.model.Borrow;
import librarymanagement.vn.library.domain.model.Borrow_;
import librarymanagement.vn.library.domain.model.Librarian;
import librarymanagement.vn.library.domain.model.Librarian_;
import librarymanagement.vn.library.domain.model.Member;
import librarymanagement.vn.library.domain.model.Member_;
import librarymanagement.vn.library.util.constant.BorrowStatus;

@Service
public class BorrowSpecs {
    public Specification<Borrow> belongsToMember(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                Join<Borrow, Member> joinBorrowMemberTable = root.join(Borrow_.MEMBER);
                return criteriaBuilder.like(criteriaBuilder.lower(joinBorrowMemberTable.get(Member_.name)),
                        "%" + name.toLowerCase() + "%");

            }
            return null;
        };
    }

    public Specification<Borrow> hasBook(String title) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(title)) {
                Join<Borrow, Book> joinBorrowBookTable = root.join(Borrow_.book);
                return criteriaBuilder.like(joinBorrowBookTable.get(Book_.TITLE), title);
            }
            return null;
        };
    }

    public Specification<Borrow> hasStatus(BorrowStatus borrowStatus) {
        return (root, criteriaQuery, criteriaBuilder) -> {

            if (borrowStatus != null) {
                return criteriaBuilder.equal(root.get(Borrow_.STATUS), borrowStatus);
            }
            return null;
        };
    }

    public Specification<Borrow> managedByLibrarian(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                Join<Borrow, Librarian> joinBorrowLibrarianTable = root.join(Borrow_.LIBRARIAN);
                return criteriaBuilder.like(criteriaBuilder.lower(joinBorrowLibrarianTable.get(Librarian_.name)),
                        "%" + name.toLowerCase() + "%");

            }
            return null;
        };

    }
}
