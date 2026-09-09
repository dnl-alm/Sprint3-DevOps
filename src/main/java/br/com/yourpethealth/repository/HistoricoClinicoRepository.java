package br.com.yourpethealth.repository;

import br.com.yourpethealth.entity.HistoricoClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoClinicoRepository extends JpaRepository<HistoricoClinico, Long> {

    List<HistoricoClinico> findByPetId(Long petId);
}