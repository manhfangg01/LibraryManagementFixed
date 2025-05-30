package librarymanagement.vn.library.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeAdminController {
    @GetMapping("/admin/dashboard")
    public String getAdminHome() {
        return "/admin/dashboard/show";
    }

}
