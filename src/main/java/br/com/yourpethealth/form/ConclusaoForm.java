package br.com.yourpethealth.form;

import br.com.yourpethealth.dto.request.ConsultaConclusaoRequest;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConclusaoForm {

    @NotBlank(message = "Observações são obrigatórias")
    @Size(min = 10, max = 1000, message = "Observações devem ter entre 10 e 1000 caracteres")
    private String observacoes;

    public ConsultaConclusaoRequest paraRequest() {
        return new ConsultaConclusaoRequest(observacoes);
    }
}