package br.com.yourpethealth.dto.request;

import jakarta.validation.constraints.*;

public record VeterinarioRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
        String email,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @NotBlank(message = "CRMV é obrigatório")
        @Size(max = 20, message = "CRMV deve ter no máximo 20 caracteres")
        String crmv,

        @Size(max = 100, message = "Especialidade deve ter no máximo 100 caracteres")
        String especialidade
) {}