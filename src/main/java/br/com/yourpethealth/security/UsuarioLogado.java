package br.com.yourpethealth.security;

import br.com.yourpethealth.entity.Usuario;
import br.com.yourpethealth.entity.enums.Perfil;
import br.com.yourpethealth.exception.AcessoNegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioLogado {

    public Usuario get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Anônimo tem principal String "anonymousUser" — o cast direto quebraria.
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new AcessoNegadoException("Nenhum usuário autenticado");
        }
        return usuario;
    }

    public Long id() {
        return get().getId();
    }

    public Perfil perfil() {
        return get().getPerfil();
    }

    public boolean ehVeterinario() {
        return perfil() == Perfil.VETERINARIO;
    }

    public boolean ehResponsavel() {
        return perfil() == Perfil.RESPONSAVEL;
    }

    /** Id do responsável logado. Lança se quem chama não é responsável. */
    public Long responsavelId() {
        Long id = get().getResponsavelId();
        if (id == null) {
            throw new AcessoNegadoException("Esta operação é exclusiva de responsáveis");
        }
        return id;
    }

    /** Id do veterinário logado. Lança se quem chama não é veterinário. */
    public Long veterinarioId() {
        Long id = get().getVeterinarioId();
        if (id == null) {
            throw new AcessoNegadoException("Esta operação é exclusiva de veterinários");
        }
        return id;
    }
}