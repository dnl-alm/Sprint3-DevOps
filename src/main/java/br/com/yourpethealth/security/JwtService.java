package br.com.yourpethealth.security;

import br.com.yourpethealth.entity.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class JwtService {

    private final Algorithm algoritmo;
    private final JWTVerifier verificador;
    private final long expiracaoHoras;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiracao-horas}") long expiracaoHoras) {
        this.algoritmo = Algorithm.HMAC256(segredo);
        this.verificador = JWT.require(algoritmo).build();
        this.expiracaoHoras = expiracaoHoras;
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();

        return JWT.create()
                .withSubject(usuario.getEmail())
                .withClaim("uid", usuario.getId())
                .withClaim("perfil", usuario.getPerfil().name())
                .withClaim("responsavelId", usuario.getResponsavelId())
                .withClaim("veterinarioId", usuario.getVeterinarioId())
                .withIssuedAt(agora)
                .withExpiresAt(agora.plus(expiracaoHoras, ChronoUnit.HOURS))
                .sign(algoritmo);
    }

    /** E-mail do token, ou vazio se inválido, expirado ou adulterado. */
    public Optional<String> extrairEmail(String token) {
        try {
            return Optional.ofNullable(verificador.verify(token).getSubject());
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    public LocalDateTime calcularExpiracao() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusHours(expiracaoHoras);
    }
}
