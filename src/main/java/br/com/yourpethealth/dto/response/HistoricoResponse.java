package br.com.yourpethealth.dto.response;

import br.com.yourpethealth.entity.HistoricoClinico;
import br.com.yourpethealth.entity.enums.TipoHistorico;
import java.time.LocalDateTime;

public record HistoricoResponse(
        Long id,
        Long petId,
        TipoHistorico tipo,
        String descricao,
        LocalDateTime data
) {
    public static HistoricoResponse from(HistoricoClinico h) {
        return new HistoricoResponse(
                h.getId(), h.getPet().getId(), h.getTipo(), h.getDescricao(), h.getData());
    }
}