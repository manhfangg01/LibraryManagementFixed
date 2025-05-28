package librarymanagement.vn.library.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import librarymanagement.vn.library.domain.dto.LibrarianFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Librarian;
import librarymanagement.vn.library.domain.repository.LibrarianRepository;
import librarymanagement.vn.library.domain.service.specification.LibrarianSpecs;

@Service
public class LibrarianService {

    private final LibrarianRepository librarianRepository;
    private final LibrarianSpecs librarianSpecs;

    public LibrarianService(LibrarianRepository librarianRepository, LibrarianSpecs librarianSpecs) {
        this.librarianRepository = librarianRepository;
        this.librarianSpecs = librarianSpecs;
    }

    public List<Librarian> fetchAllLibrarian() {
        return this.librarianRepository.findAll();
    }

    public void create(Librarian librarian) {
        this.librarianRepository.save(librarian);
    }

    public Optional<Librarian> fetchLibrarianById(long id) {
        return this.librarianRepository.findById(id);
    }

    public void update(Librarian librarian) {
        Librarian realLibrarian = this.librarianRepository.findById(librarian.getId()).get();
        realLibrarian.setEmail(librarian.getEmail());
        realLibrarian.setBorrows(librarian.getBorrows());
        realLibrarian.setName(librarian.getName());
        realLibrarian.setRole(librarian.getRole());
        realLibrarian.setImageUrl(librarian.getImageUrl());
        this.librarianRepository.save(realLibrarian);
    }

    public void delete(Librarian librarian) {
        this.librarianRepository.delete(librarian);
    }

    public Page<Librarian> fetchAllLibrariansWithPagination(Pageable pageable) {
        return librarianRepository.findAll(pageable);
    }

    public Page<Librarian> fetchAllLibrariansWithPaginationAndSpecification(
            LibrarianFilterCriteriaDTO librarianFilterCriteriaDTO, Pageable pageable) {
        Specification<Librarian> spec = Specification.where(null);
        Specification<Librarian> spec1 = this.librarianSpecs.emailLike(librarianFilterCriteriaDTO.getEmail());
        Specification<Librarian> spec2 = this.librarianSpecs.hasEmail(librarianFilterCriteriaDTO.getName());
        spec = spec.and(spec1).and(spec2);
        return this.librarianRepository.findAll(spec, pageable);
    }

}
