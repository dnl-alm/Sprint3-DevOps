package br.com.yourpethealth.controller.web;

import br.com.yourpethealth.entity.enums.Perfil;
import br.com.yourpethealth.exception.RegraNegocioException;
import br.com.yourpethealth.form.CadastroForm;
import br.com.yourpethealth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;

    /** O POST /login é tratado pelo Spring Security, não por este controller. */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String formularioCadastro(Model model) {
        model.addAttribute("form", new CadastroForm());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroForm form,
                            BindingResult resultado,
                            RedirectAttributes flash) {

        // Obrigatoriedade condicional que o Bean Validation não expressa.
        if (form.getPerfil() == Perfil.VETERINARIO
                && (form.getCrmv() == null || form.getCrmv().isBlank())) {
            resultado.rejectValue("crmv", "obrigatorio",
                    "CRMV é obrigatório para veterinários");
        }

        if (resultado.hasErrors()) {
            return "cadastro";
        }

        try {
            // Mesmo service do POST /api/auth/register.
            // O token devolvido é descartado: no web a sessão vem do form login.
            authService.registrar(form.paraRequest());
        } catch (RegraNegocioException e) {
            resultado.reject("cadastro", e.getMessage());
            return "cadastro";
        }

        flash.addFlashAttribute("sucesso", "Cadastro realizado. Faça login para continuar.");
        return "redirect:/login";
    }

    @GetMapping("/403")
    public String acessoNegado() {
        return "403";
    }

    @ModelAttribute("perfis")
    public Perfil[] perfis() {
        return new Perfil[]{Perfil.RESPONSAVEL, Perfil.VETERINARIO};
    }
}