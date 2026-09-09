package br.com.yourpethealth.dto.request;

import br.com.yourpethealth.entity.enums.Perfil;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter no mínimo 8 caracteres")
        String senha,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @NotNull(message = "Perfil é obrigatório")
        Perfil perfil,

        // obrigatórios quando perfil = VETERINARIO — validado no AuthService
        @Size(max = 20, message = "CRMV deve ter no máximo 20 caracteres")
        String crmv,

        @Size(max = 100, message = "Especialidade deve ter no máximo 100 caracteres")
        String especialidade
) {}