package br.com.yourpethealth.repository;

import br.com.yourpethealth.entity.Consulta;
import br.com.yourpethealth.entity.enums.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPetId(Long petId);

    List<Consulta> findByPetResponsavelIdOrderByDataDesc(Long responsavelId);

    List<Consulta> findByVeterinarioIdAndDataBetweenOrderByData(
            Long veterinarioId, LocalDateTime inicio, LocalDateTime fim);

    long countByPetId(Long petId);

    Optional<Consulta> findFirstByPetIdAndStatusAndDataAfterOrderByDataAsc(
            Long petId, StatusConsulta status, LocalDateTime referencia);

    boolean existsByPetIdAndStatus(Long petId, StatusConsulta status);

    List<Consulta> findByVeterinarioIdAndStatusAndDataBetween(Long veterinarioId, StatusConsulta status,
                                                              LocalDateTime inicio, LocalDateTime fim);

    List<Consulta> findByPetIdAndStatusAndData(Long petId, StatusConsulta status, LocalDateTime data);
}