package br.com.yourpethealth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErroResponse(
        LocalDateTime timestamp, int status, String erro,
        String mensagem, String path, List<CampoErro> campos
) {
    public ApiErroResponse {
        timestamp = timestamp.truncatedTo(ChronoUnit.SECONDS);
    }

    public record CampoErro(String campo, String mensagem) {}
}