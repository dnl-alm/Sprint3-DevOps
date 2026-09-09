package br.com.yourpethealth.dto.response;

import br.com.yourpethealth.entity.Consulta;
import br.com.yourpethealth.entity.enums.StatusConsulta;
import br.com.yourpethealth.entity.enums.TipoConsulta;
import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        Long petId,
        String petNome,
        Long veterinarioId,
        String veterinarioNome,
        String veterinarioEspecialidade,
        TipoConsulta tipo,
        String descricao,
        LocalDateTime data,
        StatusConsulta status,
        String observacoes,
        boolean podeEditar,
        boolean podeCancelar
) {
    public static ConsultaResponse from(Consulta c) {
        boolean editavel = c.getStatus() == StatusConsulta.AGENDADA
                && c.getData().isAfter(LocalDateTime.now().plusHours(24));

        return new ConsultaResponse(
                c.getId(),
                c.getPet().getId(),
                c.getPet().getNome(),
                c.getVeterinario().getId(),
                c.getVeterinario().getNome(),
                c.getVeterinario().getEspecialidade(),
                c.getTipo(),
                c.getDescricao(),
                c.getData(),
                c.getStatus(),
                c.getObservacoes(),
                editavel,
                editavel
        );
    }
}