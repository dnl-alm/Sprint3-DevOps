package br.com.yourpethealth.dto.response;

import br.com.yourpethealth.entity.Usuario;
import br.com.yourpethealth.entity.enums.Perfil;

public record UsuarioResponse(
        Long id, String nome, String email, Perfil perfil,
        Long responsavelId, Long veterinarioId
) {
    public static UsuarioResponse from(Usuario u, String nome) {
        return new UsuarioResponse(u.getId(), nome, u.getEmail(), u.getPerfil(),
                u.getResponsavelId(), u.getVeterinarioId());
    }
}