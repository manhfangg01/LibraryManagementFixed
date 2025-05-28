package librarymanagement.vn.library.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import librarymanagement.vn.library.domain.dto.BorrowFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Borrow;
import librarymanagement.vn.library.domain.service.BookService;
import librarymanagement.vn.library.domain.service.BorrowService;
import librarymanagement.vn.library.domain.service.LibrarianService;
import librarymanagement.vn.library.domain.service.MemberService;
import librarymanagement.vn.library.util.constant.BorrowStatus;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BorrowController {

    private final BorrowService borrowService;
    private final BookService bookService;
    private final MemberService memberService;
    private final LibrarianService librarianService;

    public BorrowController(BorrowService borrowService, BookService bookService, MemberService memberService,
            LibrarianService librarianService) {
        this.borrowService = borrowService;
        this.bookService = bookService;
        this.memberService = memberService;
        this.librarianService = librarianService;
    }

    @GetMapping("/borrows")
    public String getBorrows(
            Model model,
            @RequestParam(value = "page", defaultValue = "1") int page, // Đặt giá trị mặc định là 1 (1-indexed cho
                                                                        // người dùng)
            @RequestParam(value = "size", defaultValue = "5") int size,
            @ModelAttribute BorrowFilterCriteriaDTO borrowFilterCriteriaDTO) {

        // Chuyển đổi page từ 1-indexed (frontend) sang 0-indexed (Pageable)
        // Đảm bảo page index không bao giờ nhỏ hơn 0
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size); // Đã sửa lỗi ở đây

        Page<Borrow> borrowPage = borrowService.fetchAllBorrowsWithPagination(borrowFilterCriteriaDTO, pageable);
        List<Borrow> borrows = borrowPage.getContent();

        model.addAttribute("borrows", borrows);
        model.addAttribute("currentPage", page); // Hiển thị trang hiện tại (1-indexed)
        model.addAttribute("sizePerPage", size);
        model.addAttribute("totalPages", borrowPage.getTotalPages());

        // Rất quan trọng: truyền DTO trở lại view để giữ giá trị trong form
        model.addAttribute("borrowFilterCriteriaDTO", borrowFilterCriteriaDTO);

        // Truyền tất cả các giá trị của enum BorrowStatus để populate dropdown
        model.addAttribute("allBorrowStatuses", Arrays.asList(BorrowStatus.values()));

        return "/borrows/show";
    }

    @GetMapping("/borrows/create")
    public String getCreateBorrowPage(Model model) {
        model.addAttribute("borrow", new Borrow());
        model.addAttribute("books", this.bookService.fetchAllBooks());
        model.addAttribute("members", this.memberService.fetchAllMember());
        model.addAttribute("librarians", this.librarianService.fetchAllLibrarian());
        return "borrows/create";
    }

    @PostMapping("/borrows/create")
    public String createBorrow(@ModelAttribute("borrow") Borrow borrow) {
        this.borrowService.create(borrow);
        return "redirect:/borrows";
    }

    @GetMapping("/borrows/edit/{id}")
    public String getUpdateBorrowPage(@PathVariable("id") long id, Model model) {
        Optional<Borrow> optionalBorrow = this.borrowService.fetchBorrowById(id);
        if (optionalBorrow.isEmpty()) {
            throw new RuntimeException("Lượt mượn này không tồn tại");
        }
        Borrow borrow = optionalBorrow.get();
        model.addAttribute("statuses", BorrowStatus.values());
        model.addAttribute("borrow", borrow);
        model.addAttribute("books", this.bookService.fetchAllBooks());
        model.addAttribute("members", this.memberService.fetchAllMember());
        model.addAttribute("librarians", this.librarianService.fetchAllLibrarian());
        return "borrows/edit";
    }

    @PostMapping("/borrows/edit")
    public String updateBorrow(@ModelAttribute("borrow") Borrow borrow) {
        this.borrowService.update(borrow);
        return "redirect:/borrows";

    }

    @PostMapping("/borrows/delete/{id}")
    public String deleteBorrow(@PathVariable("id") long id) {
        this.borrowService.delete(id);

        return "redirect:/borrows";

    }

}
