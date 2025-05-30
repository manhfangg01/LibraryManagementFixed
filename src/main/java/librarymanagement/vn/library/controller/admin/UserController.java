package librarymanagement.vn.library.controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import librarymanagement.vn.library.domain.dto.UserFilterDTO;
import librarymanagement.vn.library.domain.model.Member;
import librarymanagement.vn.library.domain.model.auth.User;
import librarymanagement.vn.library.domain.service.RoleService;
import librarymanagement.vn.library.domain.service.UserService;
import librarymanagement.vn.library.domain.service.azure.AzureBlobService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserController {
    private final UserService userService;
    private final RoleService roleService;
    private final AzureBlobService azureBlobService;

    public UserController(UserService userService, RoleService roleService, AzureBlobService azureBlobService) {
        this.userService = userService;
        this.roleService = roleService;
        this.azureBlobService = azureBlobService;
    }

    @GetMapping("/admin/users")
    public String getAllUsers(
            Model model,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "acs") String sortDir,
            @ModelAttribute UserFilterDTO userFilterDTO) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        Page<User> userPages = this.userService.fetchAllUserWithSpecificationAndPagination(userFilterDTO, pageable);
        List<User> users = userPages.getContent();

        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("sizePerPage", size);
        model.addAttribute("totalPages", userPages.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "/admin/users/show";
    }

    @GetMapping("/admin/users/create")
    public String getCreateUserPage(Model model) {
        if (this.roleService.fetchAllRoles().size() <= 0) {
            System.out.println("Gay");
        }
        model.addAttribute("roles", this.roleService.fetchAllRoles());
        model.addAttribute("user", new User());
        return "/admin/users/create";
    }

    @PostMapping("/admin/users/create")
    public String createUser(Model model,
            @ModelAttribute("user") User user,
            @RequestParam("profileImage") MultipartFile file) {

        try {
            if (file != null && !file.isEmpty()) {
                String imageUrl = azureBlobService.uploadFile(file);
                user.setProfilePhoto(imageUrl);
            } else {
                Optional<User> existingMember = userService.fetchUserById(user.getId());
                if (existingMember.isPresent()) {
                    user.setProfilePhoto(existingMember.get().getProfilePhoto());
                }
            }
            this.userService.create(user);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/edit/{id}")
    public String getMethodName(Model model,
            @PathVariable("id") long id) {
        Optional<User> optionalUser = this.userService.fetchUserById(id);
        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "Không tồn tại user này");
            return "redirect:/admin/users";
        }
        model.addAttribute("user", optionalUser.get());
        model.addAttribute("roles", this.roleService.fetchAllRoles());
        return "/admin/users/edit";

    }

    @PostMapping("/admin/users/edit")
    public String editUser(@RequestParam("profileImage") MultipartFile file, @ModelAttribute("user") User user,
            Model model) {

        try {
            // Xử lý upload ảnh nếu có
            if (file != null && !file.isEmpty()) {
                String imageUrl = azureBlobService.uploadFile(file);
                user.setProfilePhoto(imageUrl);
            } else {
                // Nếu là cập nhật và không có file mới, giữ nguyên ảnh cũ
                Optional<User> existingMember = userService.fetchUserById(user.getId());
                if (existingMember.isPresent()) {
                    user.setProfilePhoto(existingMember.get().getProfilePhoto());
                }
            }
            userService.update(user);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/members";
        }

        this.userService.update(user);
        return "redirect:/admin/users";

    }

    @GetMapping("/admin/users/detail/{id}")
    public String getMethodName(@PathVariable("id") long id, Model model) {
        model.addAttribute("user", this.userService.fetchUserById(id).get());
        return "/admin/users/detail";

    }

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        this.userService.delete(this.userService.fetchUserById(id).get());
        return "redirect:/admin/users";
    }

}
