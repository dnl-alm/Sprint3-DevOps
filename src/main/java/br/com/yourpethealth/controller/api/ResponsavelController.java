package br.com.yourpethealth.controller.api;

import br.com.yourpethealth.assembler.ResponsavelAssembler;
import br.com.yourpethealth.dto.request.ResponsavelRequest;
import br.com.yourpethealth.dto.response.ResponsavelResponse;
import br.com.yourpethealth.service.ResponsavelService;
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

@Tag(name = "Responsáveis")
@RestController
@RequestMapping("/api/responsaveis")
@RequiredArgsConstructor
public class ResponsavelController {

    private final ResponsavelService service;
    private final ResponsavelAssembler assembler;

    @Operation(summary = "Cadastra um responsável", responses = {
            @ApiResponse(responseCode = "201", description = "Responsável cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ResponsavelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação")
    })
    @PostMapping
    public ResponseEntity<EntityModel<ResponsavelResponse>> criar(
            @Valid @RequestBody ResponsavelRequest request) {
        // TODO J2: migra para POST /api/auth/register (cria yp_t_usuarios na mesma transação)
        var responsavel = assembler.toModel(service.criar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(responsavel);
    }

    @Operation(summary = "Lista todos os responsáveis", responses = {
            @ApiResponse(responseCode = "200", description = "Responsáveis encontrados",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = ResponsavelResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<EntityModel<ResponsavelResponse>>> listar() {
        return ResponseEntity.ok(
                service.listar().stream().map(assembler::toModel).toList());
    }

    @Operation(summary = "Busca um responsável pelo id", responses = {
            @ApiResponse(responseCode = "200", description = "Responsável encontrado",
                    content = @Content(schema = @Schema(implementation = ResponsavelResponse.class))),
            @ApiResponse(responseCode = "404", description = "Responsável não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ResponsavelResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Atualiza um responsável", responses = {
            @ApiResponse(responseCode = "200", description = "Responsável atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ResponsavelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "404", description = "Responsável não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ResponsavelResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody ResponsavelRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, request)));
    }

    @Operation(summary = "Remove um responsável", responses = {
            @ApiResponse(responseCode = "204", description = "Responsável removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Responsável não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}