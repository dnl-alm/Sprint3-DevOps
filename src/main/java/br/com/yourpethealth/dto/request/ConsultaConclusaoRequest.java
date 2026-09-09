package br.com.yourpethealth.dto.request;

import jakarta.validation.constraints.*;

public record ConsultaConclusaoRequest(

        @NotBlank(message = "Observações são obrigatórias")
        @Size(min = 10, max = 1000, message = "Observações devem ter entre 10 e 1000 caracteres")
        String observacoes
) {}