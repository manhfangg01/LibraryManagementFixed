package librarymanagement.vn.library.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import librarymanagement.vn.library.domain.dto.auth.RegisterUserDTO;
import librarymanagement.vn.library.domain.model.auth.User;
import librarymanagement.vn.library.domain.service.RoleService;
import librarymanagement.vn.library.domain.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private UserService userService;
    private final RoleService roleService;

    public AuthController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("user", new RegisterUserDTO());
        return "/auth/register";
    }

    @PostMapping("/register")
    public String createUser(@ModelAttribute("user") @Valid RegisterUserDTO userDTO, BindingResult bindingResult,
            Model model) {
        // Bước 1: Kiểm tra các lỗi validation cơ bản từ @Valid trên RegisterUserDTO
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi, trả về view để hiển thị lại form cùng với các lỗi
            return "/auth/register";
        }

        // Bước 2: Kiểm tra mật khẩu và xác nhận mật khẩu có khớp nhau không
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {

            bindingResult.rejectValue("confirmPassword", "error.user", "Mật khẩu và xác nhận mật khẩu không khớp.");

            return "/auth/register";

        }
        // Bước 3: Kiểm tra các logic nghiệp vụ khác (ví dụ: username/email đã tồn tại)
        if (userService.existsByUsername(userDTO.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Tên người dùng đã tồn tại.");
            return "/auth/register";
        }

        if (userService.existsByEmail(userDTO.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email đã tồn tại.");
            return "/auth/register";
        }

        User user = this.userService.convertDTOToUser(userDTO);
        user.setRole(this.roleService.fetchRoleByName("USER").get());
        this.userService.create(user);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/auth/login";
    }

    // @PostMapping("/login")
    // public String postLogin() {
    // return "/homepage/show";
    // }

    @GetMapping("/access-deny")
    public String getDenyPage() {
        return "/auth/deny";
    }

}
