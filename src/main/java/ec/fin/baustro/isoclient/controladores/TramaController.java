package ec.fin.baustro.isoclient.controladores;

import ec.fin.baustro.isoclient.entidades.Trama;
import ec.fin.baustro.isoclient.servicios.TramaService;
import ec.fin.baustro.isoclient.servicios.DefinicionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/tramas")
public class TramaController {

    @Autowired
    private TramaService tramaService;

    @Autowired
    private DefinicionService definicionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tramas", tramaService.findAll());
        return "tramas/list";
    }

    @GetMapping("/vista/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("trama", tramaService.findById(id));
        return "tramas/detail";
    }

    @GetMapping("/vista/new")
    public String createForm(Model model) {
        model.addAttribute("trama", new Trama());
        model.addAttribute("definiciones", definicionService.findAll());
        return "tramas/form";
    }

    @PostMapping("/vista")
    public Mono<String> save(@ModelAttribute Trama trama) {
        return tramaService.save(trama).thenReturn("redirect:/tramas");
    }

    @GetMapping("/vista/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("trama", tramaService.findById(id));
        model.addAttribute("definiciones", definicionService.findAll());
        return "tramas/form";
    }

    @PostMapping("/vista/update/{id}")
    public Mono<String> update(@PathVariable Long id, @ModelAttribute Trama trama) {
        return tramaService.findById(id)
                .flatMap(existingTrama -> {
                    existingTrama.setNombre(trama.getNombre());
                    existingTrama.setContenido(trama.getContenido());
                    existingTrama.setDefinicionId(trama.getDefinicionId());
                    return tramaService.save(existingTrama);
                }).thenReturn("redirect:/tramas");
    }

    @GetMapping("/vista/delete/{id}")
    public Mono<String> delete(@PathVariable Long id) {
        return tramaService.deleteById(id).thenReturn("redirect:/tramas");
    }
}