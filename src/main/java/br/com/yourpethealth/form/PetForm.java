package br.com.yourpethealth.form;

import br.com.yourpethealth.dto.request.PetRequest;
import br.com.yourpethealth.dto.response.PetResponse;
import br.com.yourpethealth.entity.enums.SexoPet;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PetForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Size(max = 100, message = "Raça deve ter no máximo 100 caracteres")
    private String raca;

    @NotNull(message = "Idade é obrigatória")
    @Min(value = 0, message = "Idade não pode ser negativa")
    @Max(value = 30, message = "Idade deve ser no máximo 30")
    private Integer idade;

    @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser maior que zero")
    @DecimalMax(value = "200.0", inclusive = false, message = "Peso deve ser menor que 200")
    private BigDecimal peso;

    @NotNull(message = "Sexo é obrigatório")
    private SexoPet sexo;

    public static PetForm de(PetResponse pet) {
        var form = new PetForm();
        form.nome = pet.nome();
        form.raca = pet.raca();
        form.idade = pet.idade();
        form.peso = pet.peso();
        form.sexo = pet.sexo();
        return form;
    }

    public PetRequest paraRequest() {
        return new PetRequest(nome, raca, idade, peso, sexo);
    }
}