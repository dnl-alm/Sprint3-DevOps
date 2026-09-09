package br.com.yourpethealth.dto.response;

import java.time.LocalDateTime;

public record LoginResponse(
        String token, String tipo, LocalDateTime expiraEm, UsuarioResponse usuario
) {}