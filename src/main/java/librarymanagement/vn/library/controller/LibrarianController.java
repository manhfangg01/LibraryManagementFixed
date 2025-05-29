package librarymanagement.vn.library.controller;

import java.io.IOException;
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

import librarymanagement.vn.library.domain.dto.LibrarianFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Librarian;
import librarymanagement.vn.library.domain.service.LibrarianService;
import librarymanagement.vn.library.domain.service.azure.AzureBlobService;
import librarymanagement.vn.library.domain.service.paginationAdjustment.PaginationService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LibrarianController {
    private final LibrarianService librarianService;
    private final AzureBlobService azureBlobService;
    private final PaginationService paginationService;

    public LibrarianController(LibrarianService librarianService,
            AzureBlobService azureBlobService, PaginationService paginationService) {
        this.librarianService = librarianService;
        this.azureBlobService = azureBlobService;
        this.paginationService = paginationService;
    }

    @GetMapping("/librarians")
    public String getAllLibrarians(
            Model model,
            @RequestParam("page") Optional<Integer> optionalPage,
            @RequestParam("size") Optional<Integer> optionalSize,
            @RequestParam(defaultValue = "id") String sortBy, // Mặc định sắp xếp theo 'id'
            @RequestParam(defaultValue = "asc") String sortDir, // Mặc định sắp xếp thèo tăng dần
            @ModelAttribute LibrarianFilterCriteriaDTO librarianFilterCriteriaDTO) {

        int page = optionalPage.orElse(1); // bắt đầu từ 1
        int size = optionalSize.orElse(5);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Librarian> pageLibrarians;

        pageLibrarians = librarianService.fetchAllLibrariansWithPaginationAndSpecification(librarianFilterCriteriaDTO,
                pageable);

        List<Librarian> librarians = pageLibrarians.getContent();

        model.addAttribute("librarians", librarians);
        model.addAttribute("currentPage", page);
        model.addAttribute("sizePerPage", size);
        model.addAttribute("totalPages", pageLibrarians.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        return "librarians/show"; // trang hiển thị danh sách thủ thư
    }

    @GetMapping("/librarians/create")
    public String getCreateLibrarianPage(Model model) {
        model.addAttribute("librarian", new Librarian());
        return "/librarians/create";
    }

    @PostMapping("/librarians/create")
    public String saveLibrarian(@ModelAttribute Librarian librarian,
            @RequestParam(value = "profileImage", required = false) MultipartFile file,
            @RequestParam(value = "page", defaultValue = "1") int page, // Thêm tham số page
            @RequestParam(value = "size", defaultValue = "5") int size) {
        try {
            // Xử lý upload ảnh nếu có
            if (file != null && !file.isEmpty()) {
                String imageUrl = azureBlobService.uploadFile(file);
                librarian.setImageUrl(imageUrl);
            } else if (librarian.getId() != null) {
                // Nếu là cập nhật và không có file mới, giữ nguyên ảnh cũ
                Optional<Librarian> existingLibrarian = librarianService.fetchLibrarianById(librarian.getId());
                if (existingLibrarian.isPresent()) {
                    librarian.setImageUrl(existingLibrarian.get().getImageUrl());
                }
            }
            librarianService.create(librarian);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/librarians";
        }
        return "redirect:/librarians";
    }

    @GetMapping("/librarians/edit/{id}")
    public String getEditLibrarianPage(@PathVariable("id") long id, Model model) {
        Optional<Librarian> optionalLibrarian = this.librarianService.fetchLibrarianById(id);
        if (optionalLibrarian.isEmpty()) {
            throw new RuntimeException("Không tồn tại thủ thư này");
        }
        model.addAttribute("librarian", optionalLibrarian.get());
        return "/librarians/edit";
    }

    @PostMapping("/librarians/edit")
    public String updateLibrarian(@ModelAttribute("librarian") Librarian librarian,
            @RequestParam("profileImage") MultipartFile profileImage) {
        try {
            // Xử lý upload ảnh nếu có
            if (profileImage != null && !profileImage.isEmpty()) {
                String imageUrl = azureBlobService.uploadFile(profileImage);
                librarian.setImageUrl(imageUrl);
            } else if (librarian.getId() != null) {
                // Nếu là cập nhật và không có file mới, giữ nguyên ảnh cũ
                Optional<Librarian> existingLibrarian = librarianService.fetchLibrarianById(librarian.getId());
                if (existingLibrarian.isPresent()) {
                    librarian.setImageUrl(existingLibrarian.get().getImageUrl());
                }
            }
            librarianService.create(librarian);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/librarians";
        }
        this.librarianService.update(librarian);

        return "redirect:/librarians";
    }

    @PostMapping("/librarians/delete/{id}")
    public String deleteLibrarian(@PathVariable("id") long id) {
        Optional<Librarian> optionalLibrarian = this.librarianService.fetchLibrarianById(id);
        if (optionalLibrarian.isEmpty()) {
            throw new RuntimeException("librarian not found");
        }
        Librarian librarian = optionalLibrarian.get();

        if (librarian.getImageUrl() != null && !librarian.getImageUrl().isEmpty()) {
            String blobName = azureBlobService.getBlobNameFromUrl(librarian.getImageUrl());
            if (blobName != null) {
                azureBlobService.deleteFile(blobName);
            }
        }
        this.librarianService.delete(this.librarianService.fetchLibrarianById(id).get());
        return "redirect:/librarians";
    }

    @GetMapping("/librarians/detail/{id}")
    public String getLibrarianDetailPage(@PathVariable("id") long id, Model model) {
        model.addAttribute("librarian", this.librarianService.fetchLibrarianById(id).get());
        return "/librarians/detail";
    }

}
