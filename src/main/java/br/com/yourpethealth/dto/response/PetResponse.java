package br.com.yourpethealth.dto.response;

import br.com.yourpethealth.entity.Pet;
import br.com.yourpethealth.entity.enums.SexoPet;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PetResponse(
        Long id,
        String nome,
        String raca,
        Integer idade,
        BigDecimal peso,
        SexoPet sexo,
        Long responsavelId,
        String responsavelNome,
        long totalConsultas,
        LocalDateTime proximaConsulta
) {
    public static PetResponse from(Pet pet, long totalConsultas, LocalDateTime proximaConsulta) {
        return new PetResponse(
                pet.getId(),
                pet.getNome(),
                pet.getRaca(),
                pet.getIdade(),
                pet.getPeso(),
                pet.getSexoPet(),
                pet.getResponsavel().getId(),
                pet.getResponsavel().getNome(),
                totalConsultas,
                proximaConsulta
        );
    }
}