package br.com.yourpethealth.controller.api;

import br.com.yourpethealth.assembler.HistoricoAssembler;
import br.com.yourpethealth.dto.response.HistoricoResponse;
import br.com.yourpethealth.service.HistoricoClinicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Histórico Clínico")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoricoClinicoController {

    private final HistoricoClinicoService service;
    private final HistoricoAssembler assembler;

    @Operation(summary = "Lista o histórico clínico de um pet", responses = {
            @ApiResponse(responseCode = "200", description = "Histórico encontrado",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = HistoricoResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Pet de outro responsável"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @GetMapping("/pets/{petId}/historico")
    public ResponseEntity<List<EntityModel<HistoricoResponse>>> listarPorPet(
            @PathVariable Long petId) {
        return ResponseEntity.ok(
                service.listarPorPet(petId).stream().map(assembler::toModel).toList());
    }

    @Operation(summary = "Busca um item do histórico pelo id", responses = {
            @ApiResponse(responseCode = "200", description = "Histórico encontrado",
                    content = @Content(schema = @Schema(implementation = HistoricoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Histórico de pet de outro responsável"),
            @ApiResponse(responseCode = "404", description = "Histórico não encontrado")
    })
    @GetMapping("/historico/{id}")
    public ResponseEntity<EntityModel<HistoricoResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }
}