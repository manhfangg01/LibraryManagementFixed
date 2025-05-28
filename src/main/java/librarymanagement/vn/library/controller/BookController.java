package librarymanagement.vn.library.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import librarymanagement.vn.library.domain.dto.BookFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Book;
import librarymanagement.vn.library.domain.model.Category;
import librarymanagement.vn.library.domain.service.BookService;
import librarymanagement.vn.library.domain.service.CategoryService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping("/books")
    public String getBooks(
            Model model,
            @RequestParam(value = "page", defaultValue = "1") int page, // Rút gọn và đặt giá trị mặc định
            @RequestParam(value = "size", defaultValue = "5") int size, // Rút gọn và đặt giá trị mặc định
            @RequestParam(defaultValue = "id") String sortBy, // Mặc định sắp xếp theo 'id'
            @RequestParam(defaultValue = "asc") String sortDir, // Mặc định sắp xếp thèo tăng dần
            @ModelAttribute BookFilterCriteriaDTO filterCriteria) { // Sử dụng DTO để gom các tham số lọc

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(0, page), size, sort);

        // Lấy tất cả các thể loại để hiển thị trong dropdown
        List<Category> allCategories = categoryService.fetchAllCategories();

        // Gọi service với các bộ lọc từ DTO
        Page<Book> pageBooks = this.bookService.fetchAllBooksWithPaginationAndNameSpecification(
                pageable,
                filterCriteria // Truyền toàn bộ đối tượng DTO BookFilterCriteria
        );
        List<Book> books = pageBooks.getContent();

        model.addAttribute("books", books);
        model.addAttribute("currentPage", page);
        model.addAttribute("sizePerPage", size);
        model.addAttribute("totalPages", pageBooks.getTotalPages());

        // Truyền đối tượng filterCriteria trở lại view để giữ lại giá trị trong form
        model.addAttribute("filterCriteria", filterCriteria);
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "books/show";
    }

    @GetMapping("/books/create")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.fetchAllCategories());
        return "books/create";
    }

    @PostMapping("/books")
    public String postCreatedBook(@ModelAttribute("book") Book book, Model model) {
        if (book != null) {
            if (this.bookService.fetchBookByTitle(book.getTitle()).isPresent()) {
                model.addAttribute("ObjectExisted", "Đã tồn tại sách này rồi");
                model.addAttribute("newbook", book); // giữ lại dữ liệu người dùng nhập
                return "/categories/create";
            } else {
                this.bookService.saveBook(book);
            }
        }
        return "redirect:/books";

    }

    @GetMapping("books/edit/{id}")
    public String getUpdatePage(@PathVariable("id") long id, Model model) {
        Book book = bookService.fetchBookById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        model.addAttribute("book", book);
        model.addAttribute("id", id);

        // Sửa chỗ này: truyền toàn bộ categories có sẵn
        model.addAttribute("categories", categoryService.fetchAllCategories());
        return "/books/edit";
    }

    @PostMapping("/books/edit")
    public String updateBook(@ModelAttribute("book") Book book, Model model) {
        Optional<Book> existingBook = bookService.fetchBookById(book.getId());

        if (existingBook.isPresent() && existingBook.get().getId() != book.getId()) {
            model.addAttribute("errors", "Đã tồn tại sách này rồi");
            model.addAttribute("book", book); // giữ lại dữ liệu người dùng nhập
            return "/books/edit";
        }
        Book realBook = existingBook.get();
        realBook.setAuthorName(book.getAuthorName());
        realBook.setBorrows(book.getBorrows());
        realBook.setCategories(book.getCategories());
        realBook.setPublishedDate(book.getPublishedDate());
        realBook.setTitle(book.getTitle());
        bookService.saveBook(realBook); // phải gọi chỗ này
        return "redirect:/books";
    }

    @PostMapping("books/delete/{id}")
    public String deleteBook(@PathVariable("id") long id) {
        if (this.bookService.fetchBookById(id).isEmpty()) {
            throw new RuntimeException("book not found");
        }
        this.bookService.deleteById(id);
        return "redirect:/books";
    }

    @GetMapping("/books/detail/{id}")
    public String showBookDetail(@PathVariable("id") long id, Model model, HttpServletRequest request) {
        Optional<Book> optionalBook = this.bookService.fetchBookById(id);
        if (optionalBook.isEmpty()) {
            // Lấy URL của trang trước đó từ header Referer
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isEmpty()) {
                // Nếu có referer, chuyển hướng về trang đó
                return "redirect:" + referer;
            } else {
                // Nếu không có referer (ví dụ: truy cập trực tiếp), chuyển hướng về trang danh
                // sách sách mặc định
                return "redirect:/books";
            }
        } else {
            model.addAttribute("book", optionalBook.get()); // Đặt tên thuộc tính là "book" để khớp với template
        }
        return "books/detail"; // Đảm bảo tên view khớp với đường dẫn file HTML: templates/books/detail.html
    }

}
