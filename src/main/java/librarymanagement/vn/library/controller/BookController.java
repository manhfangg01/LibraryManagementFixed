package librarymanagement.vn.library.controller;

import java.io.IOException;
import java.util.ArrayList;
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
import librarymanagement.vn.library.domain.model.Librarian;
import librarymanagement.vn.library.domain.model.Book;
import librarymanagement.vn.library.domain.service.BookService;
import librarymanagement.vn.library.domain.service.CategoryService;
import librarymanagement.vn.library.domain.service.azure.AzureBlobService;
import librarymanagement.vn.library.domain.service.paginationAdjustment.PaginationService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final AzureBlobService azureBlobService;

    public BookController(BookService bookService, CategoryService categoryService, AzureBlobService azureBlobService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.azureBlobService = azureBlobService;

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
        List<Category> allCategories = categoryService.fetchAllCategories();
        Page<Book> pageBooks = this.bookService.fetchAllBooksWithPaginationAndNameSpecification(
                pageable,
                filterCriteria);
        List<Book> books = pageBooks.getContent();

        model.addAttribute("currentPage", page);
        model.addAttribute("sizePerPage", size);
        model.addAttribute("books", books);
        model.addAttribute("totalPages", pageBooks.getTotalPages());
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
    public String postCreatedBook(@ModelAttribute("book") Book book, Model model,
            @RequestParam(value = "bookImages", required = false) List<MultipartFile> files) {
        // Logic này chỉ dành cho việc TẠO SÁCH MỚI

        try {
            List<String> newImageUrls = new ArrayList<>();
            // Nếu có file được cung cấp và không rỗng, tải lên
            if (files != null && !files.isEmpty() && files.stream().anyMatch(f -> !f.isEmpty())) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String imageUrl = azureBlobService.uploadFile(file);
                        newImageUrls.add(imageUrl);
                    }
                }
            }
            book.setImageUrls(newImageUrls); // Gán danh sách URL ảnh vào sách mới

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải lên ảnh: " + e.getMessage());
            model.addAttribute("book", book); // Giữ lại dữ liệu người dùng nhập
            model.addAttribute("categories", categoryService.fetchAllCategories());
            return "books/create"; // Trả về form tạo
        }

        // Kiểm tra trùng lặp tiêu đề cho sách MỚI
        if (this.bookService.fetchBookByTitle(book.getTitle()).isPresent()) {
            model.addAttribute("ObjectExisted", "Đã tồn tại sách này rồi");
            model.addAttribute("book", book); // Giữ lại dữ liệu người dùng nhập
            model.addAttribute("categories", categoryService.fetchAllCategories());
            return "books/create"; // Trả về form tạo
        }

        this.bookService.saveBook(book); // Lưu sách mới

        // Chỉ cần redirect về trang danh sách sách mặc định
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
    public String updateBook(@ModelAttribute("book") Book book, Model model,
            @RequestParam(value = "bookImages", required = false) List<MultipartFile> files) {

        Optional<Book> existingBookOpt = bookService.fetchBookById(book.getId());
        if (existingBookOpt.isEmpty()) {
            throw new RuntimeException("Book not found for update");
        }
        Book realBook = existingBookOpt.get(); // <-- Sách thực tế từ DB

        try {
            List<String> updatedImageUrls = new ArrayList<>();

            if (files != null && !files.isEmpty() && files.stream().anyMatch(f -> !f.isEmpty())) {
                // Xóa tất cả ảnh cũ
                if (realBook.getImageUrls() != null) {
                    for (String oldUrl : realBook.getImageUrls()) {
                        String oldBlobName = azureBlobService.getBlobNameFromUrl(oldUrl);
                        if (oldBlobName != null) {
                            azureBlobService.deleteFile(oldBlobName);
                        }
                    }
                }
                // Tải lên các file mới và thêm vào danh sách
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String imageUrl = azureBlobService.uploadFile(file);
                        updatedImageUrls.add(imageUrl); // <-- Đảm bảo mỗi URL được thêm vào danh sách
                    }
                }
            } else {
                // Nếu không có file mới, giữ lại ảnh cũ
                updatedImageUrls = realBook.getImageUrls();
            }

            // Gán danh sách URL đã cập nhật vào đối tượng sách thực tế
            realBook.setImageUrls(updatedImageUrls); // <-- Dòng này phải gán đúng List

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải lên ảnh: " + e.getMessage());
            model.addAttribute("book", book);
            model.addAttribute("categories", categoryService.fetchAllCategories());
            return "/books/edit";
        }

        // ... (logic kiểm tra tiêu đề và cập nhật các trường khác) ...
        realBook.setTitle(book.getTitle());
        realBook.setAuthorName(book.getAuthorName());
        realBook.setPublishedDate(book.getPublishedDate());
        realBook.setCategories(book.getCategories()); // <-- Đảm bảo categories cũng được cập nhật đúng

        bookService.saveBook(realBook); // <-- Lưu đối tượng realBook đã được cập nhật
        return "redirect:/books";
    }

    @PostMapping("books/delete/{id}")
    public String deleteBook(@PathVariable("id") long id, Model model) {
        Optional<Book> optionalBook = this.bookService.fetchBookById(id);
        if (optionalBook.isEmpty()) {
            throw new RuntimeException("Book not found");
        }
        Book book = optionalBook.get();

        // Xóa TẤT CẢ các ảnh liên quan đến sách từ Azure Blob Storage
        if (book.getImageUrls() != null && !book.getImageUrls().isEmpty()) {
            for (String imageUrl : book.getImageUrls()) {
                String blobName = azureBlobService.getBlobNameFromUrl(imageUrl);
                if (blobName != null) {
                    azureBlobService.deleteFile(blobName);
                }
            }
        }

        this.bookService.deleteById(id);

        // Chỉ cần redirect về trang danh sách sách mặc định
        return "redirect:/books";
    }

    @GetMapping("/books/detail/{id}")
    public String showBookDetail(@PathVariable("id") long id, Model model, HttpServletRequest request) {
        Optional<Book> optionalBook = this.bookService.fetchBookById(id);
        if (optionalBook.isEmpty()) {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isEmpty()) {
                return "redirect:" + referer;
            } else {
                return "redirect:/books";
            }
        } else {
            model.addAttribute("book", optionalBook.get());
        }
        return "books/detail";
    }

}
