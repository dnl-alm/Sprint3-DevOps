package br.com.yourpethealth.assembler;

import br.com.yourpethealth.controller.api.PetController;
import br.com.yourpethealth.controller.api.ResponsavelController;
import br.com.yourpethealth.dto.response.ResponsavelResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ResponsavelAssembler
        implements RepresentationModelAssembler<ResponsavelResponse, EntityModel<ResponsavelResponse>> {

    @Override
    public EntityModel<ResponsavelResponse> toModel(ResponsavelResponse resp) {
        return EntityModel.of(
                resp,
                linkTo(methodOn(ResponsavelController.class).buscarPorId(resp.id())).withSelfRel(),
                linkTo(methodOn(ResponsavelController.class).listar()).withRel("responsaveis"),
                linkTo(methodOn(ResponsavelController.class)
                        .atualizar(resp.id(), null)).withRel("atualizar"),
                linkTo(methodOn(ResponsavelController.class).remover(resp.id())).withRel("remover"),
                linkTo(methodOn(PetController.class).listar()).withRel("pets")
        );
    }
}