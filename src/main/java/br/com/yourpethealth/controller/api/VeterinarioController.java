package br.com.yourpethealth.controller.api;

import br.com.yourpethealth.assembler.VeterinarioAssembler;
import br.com.yourpethealth.dto.request.VeterinarioRequest;
import br.com.yourpethealth.dto.response.VeterinarioResponse;
import br.com.yourpethealth.service.VeterinarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Veterinários")
@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

    private final VeterinarioService service;
    private final VeterinarioAssembler assembler;

    @Operation(summary = "Cadastra um veterinário", responses = {
            @ApiResponse(responseCode = "201", description = "Veterinário cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = VeterinarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação")
    })
    @PostMapping
    public ResponseEntity<EntityModel<VeterinarioResponse>> criar(
            @Valid @RequestBody VeterinarioRequest request) {
        // TODO J2: migra para POST /api/auth/register (cria yp_t_usuarios na mesma transação)
        var vet = assembler.toModel(service.criar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(vet);
    }

    @Operation(summary = "Lista todos os veterinários", responses = {
            @ApiResponse(responseCode = "200", description = "Veterinários encontrados",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = VeterinarioResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<EntityModel<VeterinarioResponse>>> listar() {
        return ResponseEntity.ok(
                service.listar().stream().map(assembler::toModel).toList());
    }

    @Operation(summary = "Busca um veterinário pelo id", responses = {
            @ApiResponse(responseCode = "200", description = "Veterinário encontrado",
                    content = @Content(schema = @Schema(implementation = VeterinarioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veterinário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<VeterinarioResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Atualiza um veterinário", responses = {
            @ApiResponse(responseCode = "200", description = "Veterinário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = VeterinarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "404", description = "Veterinário não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<VeterinarioResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody VeterinarioRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, request)));
    }

    @Operation(summary = "Remove um veterinário", responses = {
            @ApiResponse(responseCode = "204", description = "Veterinário removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Veterinário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}