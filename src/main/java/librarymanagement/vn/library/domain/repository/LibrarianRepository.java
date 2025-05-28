package librarymanagement.vn.library.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import librarymanagement.vn.library.domain.model.Librarian;

public interface LibrarianRepository extends JpaRepository<Librarian, Long>, JpaSpecificationExecutor<Librarian> {

}
