package ec.fin.baustro.isoclient.controladores;

import ec.fin.baustro.isoclient.entidades.Definicion;
import ec.fin.baustro.isoclient.servicios.DefinicionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/definiciones")
public class DefinicionController {

    @Autowired
    private DefinicionService definicionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("definiciones", definicionService.findAll());
        return "definiciones/list";
    }

    @GetMapping("/vista/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("definicion", definicionService.findById(id));
        return "definiciones/detail";
    }

    @GetMapping("/vista/new")
    public String createForm(Model model) {
        model.addAttribute("definicion", new Definicion());
        return "definiciones/form";
    }

    @PostMapping("/vista")
    public Mono<String> save(@ModelAttribute Definicion definicion) {
        return definicionService.save(definicion).thenReturn("redirect:/definiciones");
    }

    @GetMapping("/vista/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("definicion", definicionService.findById(id));
        return "definiciones/form";
    }

    @PostMapping("/vista/update/{id}")
    public Mono<String> update(@PathVariable Long id, @ModelAttribute Definicion definicion) {
        return definicionService.findById(id)
                .flatMap(existingDefinicion -> {
                    existingDefinicion.setNombre(definicion.getNombre());
                    existingDefinicion.setDescripcion(definicion.getDescripcion());
                    return definicionService.save(existingDefinicion);
                }).thenReturn("redirect:/definiciones");
    }

    @GetMapping("/vista/delete/{id}")
    public Mono<String> delete(@PathVariable Long id) {
        return definicionService.deleteById(id).thenReturn("redirect:/definiciones");
    }
}