package br.com.yourpethealth.service;

import br.com.yourpethealth.dto.request.LoginRequest;
import br.com.yourpethealth.dto.request.RegisterRequest;
import br.com.yourpethealth.dto.response.LoginResponse;
import br.com.yourpethealth.dto.response.UsuarioResponse;
import br.com.yourpethealth.entity.Responsavel;
import br.com.yourpethealth.entity.Usuario;
import br.com.yourpethealth.entity.Veterinario;
import br.com.yourpethealth.exception.RegraNegocioException;
import br.com.yourpethealth.repository.ResponsavelRepository;
import br.com.yourpethealth.repository.UsuarioRepository;
import br.com.yourpethealth.repository.VeterinarioRepository;
import br.com.yourpethealth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ResponsavelRepository responsavelRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse registrar(RegisterRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("Já existe um usuário com este e-mail");
        }

        Usuario.UsuarioBuilder builder = Usuario.builder()
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(true);

        String nome = request.nome();

        switch (request.perfil()) {
            case RESPONSAVEL -> {
                var responsavel = responsavelRepository.save(Responsavel.builder()
                        .nome(request.nome())
                        .email(request.email())
                        .telefone(request.telefone())
                        .build());
                builder.responsavelId(responsavel.getId());
            }
            case VETERINARIO -> {
                if (request.crmv() == null || request.crmv().isBlank()) {
                    throw new RegraNegocioException("CRMV é obrigatório para veterinários");
                }
                var veterinario = veterinarioRepository.save(Veterinario.builder()
                        .nome(request.nome())
                        .email(request.email())
                        .telefone(request.telefone())
                        .crmv(request.crmv())
                        .especialidade(request.especialidade())
                        .build());
                builder.veterinarioId(veterinario.getId());
            }
            case ADMIN -> throw new RegraNegocioException(
                    "Perfil ADMIN não pode ser criado por cadastro público");
        }

        var usuario = usuarioRepository.save(builder.build());
        return montarResposta(usuario, nome);
    }

    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        var usuario = usuarioRepository.findByEmail(request.email()).orElseThrow();
        return montarResposta(usuario, nomeDe(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse dadosDoUsuario(Usuario usuario) {
        return UsuarioResponse.from(usuario, nomeDe(usuario));
    }

    private LoginResponse montarResposta(Usuario usuario, String nome) {
        return new LoginResponse(
                jwtService.gerar(usuario),
                "Bearer",
                jwtService.calcularExpiracao(),
                UsuarioResponse.from(usuario, nome));
    }

    private String nomeDe(Usuario usuario) {
        if (usuario.getResponsavelId() != null) {
            return responsavelRepository.findById(usuario.getResponsavelId())
                    .map(Responsavel::getNome).orElse(usuario.getEmail());
        }
        if (usuario.getVeterinarioId() != null) {
            return veterinarioRepository.findById(usuario.getVeterinarioId())
                    .map(Veterinario::getNome).orElse(usuario.getEmail());
        }
        return usuario.getEmail();
    }
}