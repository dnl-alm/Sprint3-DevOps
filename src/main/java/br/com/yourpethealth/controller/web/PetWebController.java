package br.com.yourpethealth.controller.web;

import br.com.yourpethealth.entity.enums.SexoPet;
import br.com.yourpethealth.form.PetForm;
import br.com.yourpethealth.service.ConsultaService;
import br.com.yourpethealth.service.HistoricoClinicoService;
import br.com.yourpethealth.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/pets")
@RequiredArgsConstructor
public class PetWebController {

    private final PetService petService;
    private final ConsultaService consultaService;
    private final HistoricoClinicoService historicoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pets", petService.listar());
        return "app/pets-lista";
    }

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("form", new PetForm());
        model.addAttribute("edicao", false);
        return "app/pets-form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") PetForm form,
                        BindingResult resultado,
                        Model model,
                        RedirectAttributes flash) {

        if (resultado.hasErrors()) {
            model.addAttribute("edicao", false);
            return "app/pets-form";
        }

        var pet = petService.criar(form.paraRequest());
        flash.addFlashAttribute("sucesso", "Pet cadastrado com sucesso.");
        return "redirect:/app/pets/" + pet.id();
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        var pet = petService.buscarPorId(id);

        // Não existe endpoint de consultas por pet — filtra a listagem do responsável.
        var consultas = consultaService.listar().stream()
                .filter(c -> c.petId().equals(id))
                .toList();

        model.addAttribute("pet", pet);
        model.addAttribute("consultas", consultas);
        return "app/pet-detalhe";
    }

    @GetMapping("/{id}/editar")
    public String formularioEdicao(@PathVariable Long id, Model model) {
        model.addAttribute("form", PetForm.de(petService.buscarPorId(id)));
        model.addAttribute("petId", id);
        model.addAttribute("edicao", true);
        return "app/pets-form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") PetForm form,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes flash) {

        if (resultado.hasErrors()) {
            model.addAttribute("petId", id);
            model.addAttribute("edicao", true);
            return "app/pets-form";
        }

        petService.atualizar(id, form.paraRequest());
        flash.addFlashAttribute("sucesso", "Pet atualizado com sucesso.");
        return "redirect:/app/pets/" + id;
    }

    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id, RedirectAttributes flash) {
        petService.remover(id);
        flash.addFlashAttribute("sucesso", "Pet removido com sucesso.");
        return "redirect:/app/pets";
    }

    @GetMapping("/{petId}/historico")
    public String historico(@PathVariable Long petId, Model model) {
        model.addAttribute("pet", petService.buscarPorId(petId));
        model.addAttribute("historicos", historicoService.listarPorPet(petId));
        return "app/historico";
    }

    @ModelAttribute("sexos")
    public SexoPet[] sexos() {
        return SexoPet.values();
    }
}