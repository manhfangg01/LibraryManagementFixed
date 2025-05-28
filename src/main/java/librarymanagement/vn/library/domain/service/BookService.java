package librarymanagement.vn.library.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import librarymanagement.vn.library.domain.dto.BookFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Book;
import librarymanagement.vn.library.domain.repository.BookRepository;
import librarymanagement.vn.library.domain.service.specification.BookSpecs;

@Service
public class BookService {
    private final BookSpecs bookSpecs;
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository, BookSpecs bookSpecs) {
        this.bookRepository = bookRepository;
        this.bookSpecs = bookSpecs;
    }

    public List<Book> fetchAllBooks() {
        return this.bookRepository.findAll();
    }

    public Book saveBook(Book book) {
        return this.bookRepository.save(book);
    }

    public Optional<Book> fetchBookByTitle(String title) {
        return this.bookRepository.findByTitle(title);
    }

    public Optional<Book> fetchBookById(long id) {
        return this.bookRepository.findById(id);
    }

    public void deleteById(long id) {
        this.bookRepository.deleteById(id);
    }

    public Page<Book> fetchAllBooksWithPagination(Pageable pageable) {

        return this.bookRepository.findAll(pageable);
    }

    public Page<Book> fetchAllBooksWithPaginationAndNameSpecification(Pageable pageable,
            BookFilterCriteriaDTO bookFilterCriteriaDTO) {
        Specification<Book> spec = Specification.where(null);
        Specification<Book> spec1 = this.bookSpecs.titleLike(bookFilterCriteriaDTO.getTitle());
        Specification<Book> spec2 = this.bookSpecs.hasAuthorName(bookFilterCriteriaDTO.getAuthor());
        Specification<Book> spec3 = this.bookSpecs.hasCategories(bookFilterCriteriaDTO.getCategoryIds());
        spec = spec.and(spec1).and(spec2).and(spec3);

        return this.bookRepository.findAll(spec, pageable);
    }

}
