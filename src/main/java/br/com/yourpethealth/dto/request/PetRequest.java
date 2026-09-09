package br.com.yourpethealth.dto.request;

import br.com.yourpethealth.entity.enums.SexoPet;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PetRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
        String nome,

        @Size(max = 100, message = "Raça deve ter no máximo 100 caracteres")
        String raca,

        @NotNull(message = "Idade é obrigatória")
        @Min(value = 0, message = "Idade não pode ser negativa")
        @Max(value = 30, message = "Idade deve ser no máximo 30")
        Integer idade,

        @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser maior que zero")
        @DecimalMax(value = "200.0", inclusive = false, message = "Peso deve ser menor que 200")
        BigDecimal peso,

        @NotNull(message = "Sexo é obrigatório")
        SexoPet sexo
) {}