package br.com.yourpethealth.dto.response;

import br.com.yourpethealth.entity.Veterinario;

public record VeterinarioResponse(
        Long id, String nome, String email, String telefone, String crmv, String especialidade
) {
    public static VeterinarioResponse from(Veterinario v) {
        return new VeterinarioResponse(v.getId(), v.getNome(), v.getEmail(),
                v.getTelefone(), v.getCrmv(), v.getEspecialidade());
    }
}