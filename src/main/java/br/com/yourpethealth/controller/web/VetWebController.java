package br.com.yourpethealth.controller.web;

import br.com.yourpethealth.form.ConclusaoForm;
import br.com.yourpethealth.service.ConsultaService;
import br.com.yourpethealth.service.HistoricoClinicoService;
import br.com.yourpethealth.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/vet")
@RequiredArgsConstructor
public class VetWebController {

    private final ConsultaService consultaService;
    private final PetService petService;
    private final HistoricoClinicoService historicoService;

    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                         Model model) {

        LocalDate dia = data != null ? data : LocalDate.now();

        model.addAttribute("data", dia);
        model.addAttribute("consultas", consultaService.agenda(dia));
        return "vet/agenda";
    }

    @GetMapping("/atender/{id}")
    public String telaAtendimento(@PathVariable Long id, Model model) {
        var consulta = consultaService.buscarPorId(id);

        model.addAttribute("consulta", consulta);
        model.addAttribute("pet", petService.buscarPorId(consulta.petId()));
        model.addAttribute("historicos", historicoService.listarPorPet(consulta.petId()));
        model.addAttribute("form", new ConclusaoForm());
        return "vet/atender";
    }

    @PostMapping("/atender/{id}")
    public String concluir(@PathVariable Long id,
                           @Valid @ModelAttribute("form") ConclusaoForm form,
                           BindingResult resultado,
                           Model model,
                           RedirectAttributes flash) {

        if (resultado.hasErrors()) {
            var consulta = consultaService.buscarPorId(id);
            model.addAttribute("consulta", consulta);
            model.addAttribute("pet", petService.buscarPorId(consulta.petId()));
            model.addAttribute("historicos", historicoService.listarPorPet(consulta.petId()));
            return "vet/atender";
        }

        consultaService.concluir(id, form.paraRequest());
        flash.addFlashAttribute("sucesso",
                "Consulta concluída. Registro adicionado ao histórico clínico.");
        return "redirect:/vet/agenda";
    }

    @GetMapping("/pets")
    public String buscarPets(@RequestParam(required = false) String nome, Model model) {
        model.addAttribute("nome", nome);
        model.addAttribute("pets",
                nome == null || nome.isBlank() ? List.of() : petService.buscarPorNome(nome));
        return "vet/buscar-pet";
    }
}