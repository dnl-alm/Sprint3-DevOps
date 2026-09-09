package br.com.yourpethealth.dto.response;

import br.com.yourpethealth.entity.Responsavel;

public record ResponsavelResponse(Long id, String nome, String email, String telefone) {
    public static ResponsavelResponse from(Responsavel r) {
        return new ResponsavelResponse(r.getId(), r.getNome(), r.getEmail(), r.getTelefone());
    }
}