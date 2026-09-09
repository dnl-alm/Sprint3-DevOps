package br.com.yourpethealth.exception;

import br.com.yourpethealth.controller.web.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "br.com.yourpethealth.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(AcessoNegadoException.class)
    public String acessoNegado() {
        return "redirect:/403";
    }

    @ExceptionHandler({IdNaoEncontradoException.class, RegraNegocioException.class,
            ValidacaoException.class})
    public String erroDeNegocio(RuntimeException ex, Model model) {
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }
}