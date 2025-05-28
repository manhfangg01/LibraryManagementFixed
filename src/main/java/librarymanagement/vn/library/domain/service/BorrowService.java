package librarymanagement.vn.library.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import librarymanagement.vn.library.domain.dto.BorrowFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Book;
import librarymanagement.vn.library.domain.model.Borrow;
import librarymanagement.vn.library.domain.repository.BorrowRepository;
import librarymanagement.vn.library.domain.service.specification.BorrowSpecs;

@Service
public class BorrowService {
    private BorrowRepository borrowRepository;
    private BorrowSpecs borrowSpecs;

    public BorrowService(BorrowRepository borrowRepository, BorrowSpecs borrowSpecs) {
        this.borrowRepository = borrowRepository;
        this.borrowSpecs = borrowSpecs;
    }

    public Optional<Borrow> fetchBorrowById(long id) {
        return this.borrowRepository.findById(id);
    }

    public List<Borrow> fetchAllBorrows() {
        return this.borrowRepository.findAll();
    }

    public void create(Borrow borrow) {
        this.borrowRepository.save(borrow);
    }

    public void update(Borrow borrow) {
        Borrow realBorrow = this.borrowRepository.findById(borrow.getId()).get();
        realBorrow.setBook(borrow.getBook());
        realBorrow.setBorrowDate(borrow.getBorrowDate());
        realBorrow.setDueDate(borrow.getDueDate());
        realBorrow.setLibrarian(borrow.getLibrarian());
        realBorrow.setMember(borrow.getMember());
        realBorrow.setReturnDate(borrow.getReturnDate());
        realBorrow.setStatus(borrow.getStatus());
        this.borrowRepository.save(borrow);
    }

    public void delete(long id) {
        Optional<Borrow> optionalBorrow = this.fetchBorrowById(id);
        if (optionalBorrow.isEmpty()) {
            throw new RuntimeException("Lượt mượn này không tồn tại");
        }
        this.borrowRepository.delete(optionalBorrow.get());
    }

    public Page<Borrow> fetchAllBorrowsWithPagination(
            BorrowFilterCriteriaDTO borrowFilterCriteriaDTO, Pageable pageable) {
        Specification<Borrow> spec = Specification.where(null);
        Specification<Borrow> spec1 = this.borrowSpecs.belongsToMember(borrowFilterCriteriaDTO.getMemberName());
        Specification<Borrow> spec2 = this.borrowSpecs.hasBook(borrowFilterCriteriaDTO.getBookTitle());
        Specification<Borrow> spec3 = this.borrowSpecs.hasStatus(borrowFilterCriteriaDTO.getStatus());
        Specification<Borrow> spec4 = this.borrowSpecs.managedByLibrarian(borrowFilterCriteriaDTO.getLibrarianName());
        spec = spec.and(spec1).and(spec2).and(spec3).and(spec4);
        return this.borrowRepository.findAll(spec, pageable);
    }
}
