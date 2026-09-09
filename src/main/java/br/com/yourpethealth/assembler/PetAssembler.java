package br.com.yourpethealth.assembler;

import br.com.yourpethealth.controller.api.HistoricoClinicoController;
import br.com.yourpethealth.controller.api.PetController;
import br.com.yourpethealth.dto.response.PetResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PetAssembler
        implements RepresentationModelAssembler<PetResponse, EntityModel<PetResponse>> {

    @Override
    public EntityModel<PetResponse> toModel(PetResponse pet) {
        return EntityModel.of(
                pet,
                linkTo(methodOn(PetController.class).buscarPorId(pet.id())).withSelfRel(),
                linkTo(methodOn(PetController.class).listar()).withRel("pets"),
                linkTo(methodOn(PetController.class).atualizar(pet.id(), null)).withRel("atualizar"),
                linkTo(methodOn(PetController.class).remover(pet.id())).withRel("remover"),
                linkTo(methodOn(HistoricoClinicoController.class)
                        .listarPorPet(pet.id())).withRel("historico")
        );
    }
}