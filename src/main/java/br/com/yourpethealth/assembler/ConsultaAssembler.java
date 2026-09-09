package br.com.yourpethealth.assembler;

import br.com.yourpethealth.controller.api.ConsultaController;
import br.com.yourpethealth.controller.api.PetController;
import br.com.yourpethealth.dto.response.ConsultaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ConsultaAssembler
        implements RepresentationModelAssembler<ConsultaResponse, EntityModel<ConsultaResponse>> {

    @Override
    public EntityModel<ConsultaResponse> toModel(ConsultaResponse consulta) {
        List<Link> links = new ArrayList<>();

        links.add(linkTo(methodOn(ConsultaController.class)
                .buscarPorId(consulta.id())).withSelfRel());
        links.add(linkTo(methodOn(ConsultaController.class)
                .listar()).withRel("consultas"));
        links.add(linkTo(methodOn(PetController.class)
                .buscarPorId(consulta.petId())).withRel("pet"));

        // Links condicionais: só aparecem quando a ação é de fato permitida.
        if (consulta.podeEditar()) {
            links.add(linkTo(methodOn(ConsultaController.class)
                    .atualizar(consulta.id(), null)).withRel("atualizar"));
        }
        if (consulta.podeCancelar()) {
            links.add(linkTo(methodOn(ConsultaController.class)
                    .cancelar(consulta.id())).withRel("cancelar"));
        }

        return EntityModel.of(consulta, links);
    }
}