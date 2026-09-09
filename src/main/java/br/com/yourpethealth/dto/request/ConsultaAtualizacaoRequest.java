package br.com.yourpethealth.dto.request;

import br.com.yourpethealth.entity.enums.TipoConsulta;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record ConsultaAtualizacaoRequest(

        @NotNull(message = "Tipo é obrigatório")
        TipoConsulta tipo,

        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao,

        @NotNull(message = "Data é obrigatória")
        @Future(message = "A data deve ser futura")
        LocalDateTime data
) {}