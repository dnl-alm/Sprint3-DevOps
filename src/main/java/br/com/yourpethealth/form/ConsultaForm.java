package br.com.yourpethealth.form;

import br.com.yourpethealth.dto.request.ConsultaAtualizacaoRequest;
import br.com.yourpethealth.dto.request.ConsultaRequest;
import br.com.yourpethealth.dto.response.ConsultaResponse;
import br.com.yourpethealth.entity.enums.TipoConsulta;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class ConsultaForm {

    @NotNull(message = "Selecione um pet")
    private Long petId;

    @NotNull(message = "Selecione um veterinário")
    private Long veterinarioId;

    @NotNull(message = "Tipo é obrigatório")
    private TipoConsulta tipo;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    // O input datetime-local envia "2026-09-14T14:30" — sem segundos.
    @NotNull(message = "Data e hora são obrigatórias")
    @Future(message = "A data deve ser futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime data;

    public static ConsultaForm de(ConsultaResponse c) {
        var form = new ConsultaForm();
        form.petId = c.petId();
        form.veterinarioId = c.veterinarioId();
        form.tipo = c.tipo();
        form.descricao = c.descricao();
        form.data = c.data();
        return form;
    }

    public ConsultaRequest paraRequest() {
        return new ConsultaRequest(petId, veterinarioId, tipo, descricao, data);
    }

    public ConsultaAtualizacaoRequest paraAtualizacao() {
        return new ConsultaAtualizacaoRequest(tipo, descricao, data);
    }
}