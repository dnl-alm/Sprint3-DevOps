package br.com.yourpethealth.controller.api;

import br.com.yourpethealth.dto.request.LoginRequest;
import br.com.yourpethealth.dto.request.RegisterRequest;
import br.com.yourpethealth.dto.response.LoginResponse;
import br.com.yourpethealth.dto.response.UsuarioResponse;
import br.com.yourpethealth.entity.Usuario;
import br.com.yourpethealth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @Operation(summary = "Cadastra um usuário e o perfil correspondente", responses = {
            @ApiResponse(responseCode = "201", description = "Cadastro realizado",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }

    @Operation(summary = "Autentica e devolve o JWT", responses = {
            @ApiResponse(responseCode = "200", description = "Autenticado",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.autenticar(request));
    }

    @Operation(summary = "Dados do usuário autenticado", responses = {
            @ApiResponse(responseCode = "200", description = "Usuário autenticado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.dadosDoUsuario(usuario));
    }
}