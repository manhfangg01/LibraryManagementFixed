package librarymanagement.vn.library.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("")
    public String getHomepage() {
        return "/client/homepage/show";
    }

}
