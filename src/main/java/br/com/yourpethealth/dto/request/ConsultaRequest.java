package br.com.yourpethealth.dto.request;

import br.com.yourpethealth.entity.enums.TipoConsulta;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record ConsultaRequest(

        @NotNull(message = "Pet é obrigatório")
        Long petId,

        @NotNull(message = "Veterinário é obrigatório")
        Long veterinarioId,

        @NotNull(message = "Tipo é obrigatório")
        TipoConsulta tipo,

        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao,

        @NotNull(message = "Data é obrigatória")
        @Future(message = "A data deve ser futura")
        LocalDateTime data
) {}