package ec.fin.baustro.isoclient.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToDefiniciones() {
        return "redirect:/definiciones";
    }
}