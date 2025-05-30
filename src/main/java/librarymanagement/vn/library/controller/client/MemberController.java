package librarymanagement.vn.library.controller.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import librarymanagement.vn.library.domain.dto.MemberFilterCriteriaDTO;
import librarymanagement.vn.library.domain.model.Member;
import librarymanagement.vn.library.domain.service.MemberService;
import librarymanagement.vn.library.domain.service.azure.AzureBlobService;
import librarymanagement.vn.library.util.constant.MembershipType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MemberController {
    private final MemberService memberService;
    private final AzureBlobService azureBlobService;

    public MemberController(MemberService memberService, AzureBlobService azureBlobService) {
        this.memberService = memberService;
        this.azureBlobService = azureBlobService;
    }

    @GetMapping("/members")
    public String getAllMembers(
            Model model,
            @RequestParam(value = "page", defaultValue = "1") int page, // Đặt giá trị mặc định là 1 (1-indexed cho
                                                                        // người dùng)
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy, // Mặc định sắp xếp theo 'id'
            @RequestParam(defaultValue = "asc") String sortDir, // Mặc định sắp xếp thèo tăng dần

            @ModelAttribute MemberFilterCriteriaDTO memberFilterCriteriaDTO) {

        // Chuyển đổi page từ 1-indexed (frontend) sang 0-indexed (Pageable)
        // Đảm bảo page index không bao giờ nhỏ hơn 0
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        Page<Member> pageMembers = this.memberService.fetchAllMembersWithPaginationAndSpecification(
                memberFilterCriteriaDTO, pageable);
        List<Member> members = pageMembers.getContent();

        // Truyền tất cả các giá trị của enum MembershipType để populate dropdown
        model.addAttribute("allMembershipTypes", Arrays.asList(MembershipType.values()));

        model.addAttribute("members", members);
        model.addAttribute("currentPage", page); // Hiển thị trang hiện tại (1-indexed)
        model.addAttribute("sizePerPage", size);
        model.addAttribute("totalPages", pageMembers.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        // Rất quan trọng: truyền DTO trở lại view để giữ giá trị trong form
        model.addAttribute("memberFilterCriteriaDTO", memberFilterCriteriaDTO);

        return "/client/members/show"; // trang hiển thị danh sách members
    }

    @GetMapping("/members/create")
    public String getCreateMemberPage(Model model) {
        model.addAttribute("allMembershipTypes", Arrays.asList(MembershipType.values()));

        model.addAttribute("member", new Member());
        return "/client/members/create";
    }

    @PostMapping("/members/create")
    public String postMethodName(@ModelAttribute("member") Member member,
            @RequestParam("profileImage") MultipartFile file, Model model) { // Lưu ý là đừng có đặt trùng tên với
                                                                             // trường String (Url của ảnh )
        model.addAttribute("allMembershipTypes", Arrays.asList(MembershipType.values()));
        try {
            // Xử lý upload ảnh nếu có
            if (file != null && !file.isEmpty()) {
                String imageUrl = azureBlobService.uploadFile(file);
                member.setImageUrl(imageUrl);
            } else if (member.getId() != null) {
                // Nếu là cập nhật và không có file mới, giữ nguyên ảnh cũ
                Optional<Member> existingMember = memberService.fetchMemberById(member.getId());
                if (existingMember.isPresent()) {
                    member.setImageUrl(existingMember.get().getImageUrl());
                }
            }
            memberService.create(member);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/members";
        }
        this.memberService.create(member);
        return "redirect:/members";
    }

    @GetMapping("/members/edit/{id}")
    public String getEditMemberPage(@PathVariable("id") long id, Model model) {
        Optional<Member> optionalMember = this.memberService.fetchMemberById(id);
        if (optionalMember.isEmpty()) {
            throw new RuntimeException("Không tồn tại thủ thư này");
        }
        model.addAttribute("allMembershipTypes", Arrays.asList(MembershipType.values()));
        model.addAttribute("member", optionalMember.get());
        return "/client/members/edit";
    }

    @PostMapping("/members/edit")
    public String updateMember(@ModelAttribute("member") Member member,
            @RequestParam("profileImage") MultipartFile file) {

        try {
            // Xử lý upload ảnh nếu có
            if (file != null && !file.isEmpty()) {
                String imageUrl = azureBlobService.uploadFile(file);
                member.setImageUrl(imageUrl);
            } else if (member.getId() != null) {
                // Nếu là cập nhật và không có file mới, giữ nguyên ảnh cũ
                Optional<Member> existingMember = memberService.fetchMemberById(member.getId());
                if (existingMember.isPresent()) {
                    member.setImageUrl(existingMember.get().getImageUrl());
                }
            }
            memberService.create(member);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/members";
        }
        this.memberService.update(member);

        return "redirect:/members";
    }

    @PostMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable("id") long id) {
        Optional<Member> optionalMember = this.memberService.fetchMemberById(id);
        if (optionalMember.isEmpty()) {
            throw new RuntimeException("member not found");
        }
        Member member = optionalMember.get();

        if (member.getImageUrl() != null && !member.getImageUrl().isEmpty()) {
            String blobName = azureBlobService.getBlobNameFromUrl(member.getImageUrl());
            if (blobName != null) {
                azureBlobService.deleteFile(blobName);
            }
        }
        this.memberService.delete(this.memberService.fetchMemberById(id).get());
        return "redirect:/members";
    }

    @GetMapping("/members/detail/{id}")
    public String getDetailPage(@PathVariable("id") long id, Model model) {
        Optional<Member> optionalMember = this.memberService.fetchMemberById(id);
        if (optionalMember.isEmpty()) {
            System.out.println("gay");
        }
        model.addAttribute("member", optionalMember.get());
        return "/client/members/detail";
    }

}
