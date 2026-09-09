package br.com.yourpethealth.assembler;

import br.com.yourpethealth.controller.api.VeterinarioController;
import br.com.yourpethealth.dto.response.VeterinarioResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VeterinarioAssembler
        implements RepresentationModelAssembler<VeterinarioResponse, EntityModel<VeterinarioResponse>> {

    @Override
    public EntityModel<VeterinarioResponse> toModel(VeterinarioResponse vet) {
        return EntityModel.of(
                vet,
                linkTo(methodOn(VeterinarioController.class).buscarPorId(vet.id())).withSelfRel(),
                linkTo(methodOn(VeterinarioController.class).listar()).withRel("veterinarios"),
                linkTo(methodOn(VeterinarioController.class)
                        .atualizar(vet.id(), null)).withRel("atualizar"),
                linkTo(methodOn(VeterinarioController.class).remover(vet.id())).withRel("remover")
        );
    }
}