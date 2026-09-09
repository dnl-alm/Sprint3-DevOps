package br.com.yourpethealth.assembler;

import br.com.yourpethealth.controller.api.HistoricoClinicoController;
import br.com.yourpethealth.controller.api.PetController;
import br.com.yourpethealth.dto.response.HistoricoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class HistoricoAssembler
        implements RepresentationModelAssembler<HistoricoResponse, EntityModel<HistoricoResponse>> {

    @Override
    public EntityModel<HistoricoResponse> toModel(HistoricoResponse historico) {
        return EntityModel.of(
                historico,
                linkTo(methodOn(HistoricoClinicoController.class)
                        .buscarPorId(historico.id())).withSelfRel(),
                linkTo(methodOn(PetController.class)
                        .buscarPorId(historico.petId())).withRel("pet")
        );
    }
}